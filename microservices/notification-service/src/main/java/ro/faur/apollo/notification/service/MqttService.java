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

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final DeviceServiceClient deviceServiceClient;
    private final MediaAnalysisServiceClient mediaAnalysisServiceClient;
    private final ExecutorService executorService;
    private MqttClient mqttClient;

    @Value("${mqtt.broker.url}")
    private String mqttBrokerUrl;
    @Value("${minio.url}")
    private String minioUrl;
    @Value("${minio.bucket}")
    private String minioBucket;

    private String linkPrefix;

    public MqttService(NotificationService notificationService,
                       ObjectMapper objectMapper,
                       DeviceServiceClient deviceServiceClient,
                       MediaAnalysisServiceClient mediaAnalysisServiceClient) throws MqttException {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.deviceServiceClient = deviceServiceClient;
        this.mediaAnalysisServiceClient = mediaAnalysisServiceClient;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @PostConstruct
    public void initialize() {
        this.linkPrefix = "http://" + minioUrl + "/" + minioBucket;
        try {
            mqttClient = new MqttClient(mqttBrokerUrl, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            mqttClient.connect(options);
            subscribeToTopics();
        } catch (MqttException e) {
            logger.error("Failed to connect to MQTT broker at {}: {}", mqttBrokerUrl, e.getMessage());
            throw new RuntimeException("Could not connect to MQTT broker", e);
        }
    }

    private void subscribeToTopics() throws MqttException {
        mqttClient.subscribe("devices/hello", this::handleHelloMessage);
        mqttClient.subscribe("devices/notifications", this::handleNotificationMessage);
        logger.info("Subscribed to MQTT topics: devices/hello and devices/notifications");
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

                if (notifMsg.getMediaUrl() != null) {
                    String mediaUrl = linkPrefix + notifMsg.getMediaUrl();
                    notificationMessage = getMediaAnalysis(mediaUrl);
                }

                Notification notification = new Notification();
                notification.setTitle(notifMsg.getTitle());
                notification.setMessage(notificationMessage);
                if (notifMsg.getMediaUrl() != null) {
                    notification.setMediaUrl(linkPrefix + notifMsg.getMediaUrl());
                }
                notification.setType(NotificationEventType.DOORLOCK_MISC);
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
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
            logger.info("Disconnected from MQTT broker");
        }
    }
} 