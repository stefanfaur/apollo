package ro.faur.apollo.libs.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.errors.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.device.repository.DeviceRepository;
import ro.faur.apollo.libs.mqtt.dto.HelloMessage;
import ro.faur.apollo.libs.mqtt.dto.NotificationMessage;
import ro.faur.apollo.notification.domain.Notification;
import ro.faur.apollo.notification.domain.types.NotificationEventType;
import ro.faur.apollo.notification.repository.NotificationRepository;

import java.nio.charset.StandardCharsets;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    private final MqttClient mqttClient;
    private final DeviceRepository deviceRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public MqttService(DeviceRepository deviceRepository,
                       NotificationRepository notificationRepository,
                       ObjectMapper objectMapper) throws MqttException {
        this.deviceRepository = deviceRepository;
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;

        // Configure the MQTT client with automatic reconnect and clean session options
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
        try {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            // Parse the JSON payload into a HelloMessage DTO
            HelloMessage helloMsg = objectMapper.readValue(payload, HelloMessage.class);
            if (helloMsg.getHardwareId() == null || helloMsg.getDeviceType() == null) {
                logger.warn("Invalid hello message: missing required fields. Payload: {}", payload);
                return;
            }
            // Check if the device already exists
            Device device = deviceRepository.findByHardwareId(helloMsg.getHardwareId());
            if (device == null) {
                // Create and save a new device.
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
    }

    private void handleNotificationMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            // Parse the JSON payload from Mqtt queue into a NotificationMessage DTO
            NotificationMessage notifMsg = objectMapper.readValue(payload, NotificationMessage.class);
            if (notifMsg.getHardwareId() == null ||
                    notifMsg.getTitle() == null ||
                    notifMsg.getMessage() == null) {
                logger.warn("Invalid notification message: missing required fields. Payload: {}", payload);
                return;
            }
            // Retrieve the device by hardwareId
            Device device = deviceRepository.findByHardwareId(notifMsg.getHardwareId());
            if (device == null || device.getName() == null) {
                logger.warn("Notification from unregistered or inactive device: {}", notifMsg.getHardwareId());
                return;
            }
            Notification notification = new Notification();
            notification.setTitle(notifMsg.getTitle());
            notification.setMessage(notifMsg.getMessage());

            // Use the provided mediaUrl if available; otherwise, use a default
            if (notifMsg.getMediaUrl() != null && !notifMsg.getMediaUrl().isEmpty()) {
                notification.setMediaUrl(notifMsg.getMediaUrl());
            } else {
                notification.setMediaUrl("https://placehold.co/300x300.jpeg");
            }
            notification.setType(NotificationEventType.DOORLOCK_MISC);
            notification.setEmitter(device);
            notificationRepository.save(notification);
            logger.info("Saved notification for device: {}", notifMsg.getHardwareId());
        } catch (Exception e) {
            logger.error("Error processing notification message", e);
        }
    }

    public void disconnect() throws MqttException {
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
            logger.info("Disconnected from MQTT broker");
        }
    }
}
