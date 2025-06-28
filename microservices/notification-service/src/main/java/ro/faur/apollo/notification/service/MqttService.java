package ro.faur.apollo.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.faur.apollo.notification.client.DeviceServiceClient;
import ro.faur.apollo.notification.client.MediaAnalysisServiceClient;
import ro.faur.apollo.notification.domain.Notification;
import ro.faur.apollo.notification.domain.NotificationEventType;
import ro.faur.apollo.notification.dto.mqtt.HelloMessage;
import ro.faur.apollo.notification.dto.mqtt.NotificationMessage;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final DeviceServiceClient deviceServiceClient;
    private final MediaAnalysisServiceClient mediaAnalysisServiceClient;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    private MqttClient mqttClient;
    private volatile boolean connected = false;

    @Value("${mqtt.broker.url}")
    private String mqttBrokerUrl;
    @Value("${minio.url}")
    private String minioUrl;
    @Value("${minio.bucket}")
    private String minioBucket;

    private String linkPrefix;
    private static final int MAX_RETRY_ATTEMPTS = 10;
    private static final long INITIAL_RETRY_DELAY_MS = 5000;

    public MqttService(NotificationService notificationService,
                       ObjectMapper objectMapper,
                       DeviceServiceClient deviceServiceClient,
                       MediaAnalysisServiceClient mediaAnalysisServiceClient) throws MqttException {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.deviceServiceClient = deviceServiceClient;
        this.mediaAnalysisServiceClient = mediaAnalysisServiceClient;
        this.executorService = Executors.newFixedThreadPool(10);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
    }

    @PostConstruct
    public void initialize() {
        // Ensure the prefix ends with a single slash so later concatenations don't miss it.
        String rawPrefix = minioUrl + "/" + minioBucket;
        this.linkPrefix = rawPrefix.endsWith("/") ? rawPrefix : rawPrefix + "/";
        
        // Start MQTT connection in background - don't fail application startup
        logger.info("Starting MQTT connection to broker at: {}", mqttBrokerUrl);
        scheduledExecutorService.execute(this::connectWithRetry);
    }

    private void connectWithRetry() {
        int attempt = 0;
        long delay = INITIAL_RETRY_DELAY_MS;

        while (attempt < MAX_RETRY_ATTEMPTS && !connected) {
            try {
                attempt++;
                logger.info("MQTT connection attempt {} of {}", attempt, MAX_RETRY_ATTEMPTS);
                
                connectToMqtt();
                connected = true;
                logger.info("Successfully connected to MQTT broker at: {}", mqttBrokerUrl);
                
            } catch (Exception e) {
                logger.warn("MQTT connection attempt {} failed: {}. Retrying in {}ms", 
                           attempt, e.getMessage(), delay);
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("MQTT connection retry interrupted");
                        return;
                    }
                    delay = Math.min(delay * 2, 60000); // Exponential backoff, max 60 seconds
                } else {
                    logger.error("Failed to connect to MQTT broker after {} attempts. MQTT functionality will be disabled.", MAX_RETRY_ATTEMPTS);
                }
            }
        }
    }

    private void connectToMqtt() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            return;
        }

        mqttClient = new MqttClient(mqttBrokerUrl, MqttClient.generateClientId(), new MemoryPersistence());
        
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);  // Force MQTT v3.1.1 like working client
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);  // Match working client's 60-second keepalive
        options.setMaxInflight(10);
        options.setServerURIs(new String[]{mqttBrokerUrl});
        
        // Set callback for connection events
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                logger.info("MQTT connection established to: {} (reconnect: {})", serverURI, reconnect);
                connected = true;
                try {
                    subscribeToTopics();
                } catch (MqttException e) {
                    logger.error("Failed to subscribe to topics after connection", e);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                logger.warn("MQTT connection lost: {}", cause.getMessage());
                connected = false;
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // This is handled by individual topic subscriptions
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Not used for subscriptions
            }
        });

        mqttClient.connect(options);
        subscribeToTopics();
    }

    private void subscribeToTopics() throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            logger.warn("Cannot subscribe to topics - MQTT client not connected");
            return;
        }
        
        mqttClient.subscribe("devices/hello", this::handleHelloMessage);
        mqttClient.subscribe("devices/notifications", this::handleNotificationMessage);
        mqttClient.subscribe("doorlock/+/enroll/status", this::handleEnrollStatusMessage);
        logger.info("Subscribed to MQTT topics: devices/hello, devices/notifications, and doorlock/+/enroll/status");
    }

    private void handleHelloMessage(String topic, MqttMessage message) {
        executorService.submit(() -> {
            try {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                HelloMessage helloMsg = objectMapper.readValue(payload, HelloMessage.class);

                if (helloMsg.getHardwareId() == null || helloMsg.getDeviceType() == null) {
                    logger.warn("Invalid hello message: missing required fields. Payload: {}", payload);
                    return;
                }

                // Call Device Service to register/update device
                registerDevice(helloMsg);

            } catch (Exception e) {
                logger.error("Error processing hello message", e);
            }
        });
    }

    private void handleNotificationMessage(String topic, MqttMessage message) {
        executorService.submit(() -> {
            try {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                NotificationMessage notifMsg = objectMapper.readValue(payload, NotificationMessage.class);

                if (notifMsg.getHardwareId() == null) {
                    logger.warn("Invalid notification message: missing required fields. Payload: {}", payload);
                    return;
                }

                String deviceUuid = getDeviceUuidByHardwareId(notifMsg.getHardwareId());
                if (deviceUuid == null) {
                    logger.warn("Notification from unregistered device: {}", notifMsg.getHardwareId());
                    return;
                }

                String notificationMessage = notifMsg.getMessage();

                // Analyse media content with AI service, if any
                if (notifMsg.getMediaUrl() != null && !notifMsg.getMediaUrl().isBlank()) {
                    String mediaPath = notifMsg.getMediaUrl();
                    // Strip any leading slash so we don't end up with double slashes
                    if (mediaPath.startsWith("/")) {
                        mediaPath = mediaPath.substring(1);
                    }
                    String mediaUrl = linkPrefix + mediaPath;
                    notificationMessage = getMediaAnalysis(mediaUrl);
                }

                // Map event type coming from device to our internal enum and derive a better title
                String eventTypeStr = notifMsg.getEventType();
                NotificationEventType mappedType = mapEventType(eventTypeStr);


                String originalTitle = notifMsg.getTitle();
                String title;
                if (originalTitle == null || originalTitle.equalsIgnoreCase("Video Recording Alert")) {
                    title = (eventTypeStr != null && !eventTypeStr.isBlank()) ? capitalize(eventTypeStr) : "Security Event";
                } else {
                    title = originalTitle;
                }

                Notification notification = new Notification();
                notification.setTitle(title);
                notification.setMessage(notificationMessage);
                if (notifMsg.getMediaUrl() != null) {
                    String mediaPath = notifMsg.getMediaUrl();
                    if (mediaPath.startsWith("/")) {
                        mediaPath = mediaPath.substring(1);
                    }
                    notification.setMediaUrl(linkPrefix + mediaPath);
                }
                notification.setType(mappedType);
                notification.setDeviceUuid(deviceUuid);

                notificationService.saveNotification(notification);
                logger.info("Saved AI-generated notification for device: {}", notifMsg.getHardwareId());

            } catch (Exception e) {
                logger.error("Error processing notification message", e);
            }
        });
    }

    private void registerDevice(HelloMessage helloMsg) {
        try {
            deviceServiceClient.registerDevice(helloMsg.getHardwareId(), helloMsg.getDeviceType());
            logger.info("Received hello message from device with hardwareId: {}", helloMsg.getHardwareId());
        } catch (Exception e) {
            logger.error("Error registering device with hardwareId: {}", helloMsg.getHardwareId(), e);
        }
    }

    private String getDeviceUuidByHardwareId(String hardwareId) {
        try {
            return deviceServiceClient.getDeviceUuidByHardwareId(hardwareId);
        } catch (Exception e) {
            logger.error("Error getting device UUID for hardwareId: {}", hardwareId, e);
            return null;
        }
    }

    private String getMediaAnalysis(String mediaUrl) {
        try {
            return mediaAnalysisServiceClient.analyzeMedia(mediaUrl);
        } catch (Exception e) {
            logger.error("Error getting media analysis for URL: {}", mediaUrl, e);
            return "Error analyzing media content";
        }
    }

    public void disconnect() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
            logger.info("Disconnected from MQTT broker");
        }
        executorService.shutdown();
        scheduledExecutorService.shutdown();
    }

    // Publish enroll start command on behalf of Device Service
    public void publishEnrollStart(String hardwareId, int userFpId) {
        if (!isConnected()) {
            logger.warn("Cannot publish enroll start - MQTT not connected");
            return;
        }
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("hardwareId", hardwareId);
        payload.put("user_fp_id", userFpId);
        try {
            String json = objectMapper.writeValueAsString(payload);
            MqttMessage mqttMessage = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
            mqttClient.publish("doorlock/1/enroll/start", mqttMessage);
            logger.info("Published enroll start for hardwareId {} (templateId={})", hardwareId, userFpId);
        } catch (Exception e) {
            logger.error("Failed to publish enroll start", e);
        }
    }

    private void handleEnrollStatusMessage(String topic, MqttMessage message) {
        executorService.submit(() -> {
            try {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                Map<?,?> map = objectMapper.readValue(payload, Map.class);
                String hardwareId = (String) map.get("hardwareId");
                String eventType = (String) map.get("eventType");
                if (hardwareId == null || eventType == null) return;

                String deviceUuid = getDeviceUuidByHardwareId(hardwareId);
                if (deviceUuid == null) return;

                Map<String, Object> updateBody = new HashMap<>();
                if (eventType.equals("EnrollSuccess")) {
                    updateBody.put("status", "success");
                } else if (eventType.equals("EnrollFailure")) {
                    updateBody.put("status", "failure");
                    updateBody.put("errorCode", map.get("description"));
                } else {
                    return;
                }

                deviceServiceClient.updateEnrollStatus(deviceUuid, updateBody);
            } catch (Exception e) {
                logger.error("Error processing enroll status message", e);
            }
        });
    }

    /**
     * Maps the free-form event type string coming from the IoT device into our internal
     * NotificationEventType enum. The mapping is deliberately fuzzy – it relies on keywords
     * to keep the firmware and the backend loosely coupled while still enabling us to
     * categorise notifications properly.
     */
    private NotificationEventType mapEventType(String eventType) {
        if (eventType == null) {
            return NotificationEventType.DOORLOCK_MISC;
        }

        String normalized = eventType.toLowerCase();

        if (normalized.contains("unauthorized") && normalized.contains("door")) {
            return NotificationEventType.DOORLOCK_OPENED_UNAUTHORIZED;
        }
        if (normalized.contains("door") && normalized.contains("handle")) {
            return NotificationEventType.DOORLOCK_HANDLE_TRIED_UNAUTHORIZED;
        }
        if (normalized.contains("door") && normalized.contains("opened")) {
            return NotificationEventType.DOORLOCK_OPENED_AUTHORIZED;
        }
        if (normalized.contains("motion")) {
            return NotificationEventType.DOORLOCK_SUSPICIOUS_ACTIVITY;
        }
        if (normalized.contains("fingerprint")) {
            return NotificationEventType.DOORLOCK_HANDLE_TRIED_UNAUTHORIZED;
        }

        return NotificationEventType.DOORLOCK_MISC;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public void publishUnlock(String hardwareId) {
        if (!isConnected()) {
            logger.warn("Cannot publish unlock command - MQTT not connected");
            return;
        }
        
        try {
            String json = objectMapper.writeValueAsString(Map.of("hardwareId", hardwareId));
            MqttMessage msg = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
            mqttClient.publish("devices/commands/unlock", msg);
            logger.info("Published remote unlock for {}", hardwareId);
        } catch (Exception e) {
            logger.error("Failed to publish unlock", e);
        }
    }
    
    public boolean isConnected() {
        return connected && mqttClient != null && mqttClient.isConnected();
    }
} 