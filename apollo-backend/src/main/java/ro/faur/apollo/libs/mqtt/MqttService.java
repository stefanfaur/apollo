package ro.faur.apollo.libs.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.device.repository.DeviceRepository;
import ro.faur.apollo.libs.images.analyzer.ImageProcessorService;
import ro.faur.apollo.libs.images.analyzer.QwenApiService;
import ro.faur.apollo.libs.mqtt.dto.HelloMessage;
import ro.faur.apollo.libs.mqtt.dto.NotificationMessage;
import ro.faur.apollo.notification.domain.Notification;
import ro.faur.apollo.notification.domain.types.NotificationEventType;
import ro.faur.apollo.notification.repository.NotificationRepository;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    private final MqttClient mqttClient;
    private final DeviceRepository deviceRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final ImageProcessorService imageProcessorService;
    private final QwenApiService qwenApiService;
    private final ExecutorService executorService;

    private String linkPrefix = "http://localhost:9000/apollo-bucket/";

    public MqttService(DeviceRepository deviceRepository,
                       NotificationRepository notificationRepository,
                       ObjectMapper objectMapper,
                       ImageProcessorService imageProcessorService,
                       QwenApiService qwenApiService) throws MqttException {
        this.deviceRepository = deviceRepository;
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
        this.imageProcessorService = imageProcessorService;
        this.qwenApiService = qwenApiService;

        // Use a thread pool to handle multiple incoming notifications in parallel
        this.executorService = Executors.newFixedThreadPool(10);

        mqttClient = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        mqttClient.connect(options);
        subscribeToTopics();
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
                Device device = deviceRepository.findByHardwareId(helloMsg.getHardwareId());
                if (device == null) {
                    device = new Device();
                    device.setHardwareId(helloMsg.getHardwareId());
                    device.setDeviceType(helloMsg.getDeviceType());
                    deviceRepository.save(device);
                    logger.info("Registered new device with hardwareId: {}", helloMsg.getHardwareId());
                } else {
                    logger.info("Device already registered: {}", helloMsg.getHardwareId());
                }
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

                if (notifMsg.getHardwareId() == null || notifMsg.getMediaUrl() == null) {
                    logger.warn("Invalid notification message: missing required fields. Payload: {}", payload);
                    return;
                }

                Device device = deviceRepository.findByHardwareId(notifMsg.getHardwareId());
                if (device == null) {
                    logger.warn("Notification from unregistered device: {}", notifMsg.getHardwareId());
                    return;
                }

                String notificationMessage = notifMsg.getMessage();

                if (imageProcessorService.isImage(linkPrefix + notifMsg.getMediaUrl())) {
                    String base64Image = imageProcessorService.downloadAndConvertToBase64(linkPrefix + notifMsg.getMediaUrl());
                    notificationMessage = qwenApiService.getDescriptionFromImage(base64Image);
                }

                Notification notification = new Notification();
                notification.setTitle(notifMsg.getTitle());
                notification.setMessage(notificationMessage);
                notification.setMediaUrl(linkPrefix + notifMsg.getMediaUrl());
                notification.setType(NotificationEventType.DOORLOCK_MISC);
                notification.setEmitter(device);
                notificationRepository.save(notification);
                logger.info("Saved AI-generated notification for device: {}", notifMsg.getHardwareId());

            } catch (Exception e) {
                logger.error("Error processing notification message", e);
            }
        });
    }

    public void disconnect() throws MqttException {
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
            logger.info("Disconnected from MQTT broker");
        }
    }
}
