package ro.faur.apollo.libs.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.device.repository.DeviceRepository;
import ro.faur.apollo.notification.domain.Notification;
import ro.faur.apollo.notification.domain.types.NotificationEventType;
import ro.faur.apollo.notification.repository.NotificationRepository;

@Service
public class MqttService {

    private final MqttClient mqttClient;
    private final DeviceRepository deviceRepository;
    private final NotificationRepository notificationRepository;

    public MqttService(DeviceRepository deviceRepository, NotificationRepository notificationRepository) throws MqttException {
        this.deviceRepository = deviceRepository;
        this.notificationRepository = notificationRepository;

        mqttClient = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId());
        mqttClient.connect();
        subscribeToTopics();
    }

    private void subscribeToTopics() throws MqttException {
        mqttClient.subscribe("devices/hello", this::handleHelloMessage);
        mqttClient.subscribe("devices/notifications", this::handleNotificationMessage);
    }

    private void handleHelloMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            String[] parts = payload.split(",");
            String hardwareId = parts[0];
            String deviceType = parts[1];

            Device device = deviceRepository.findByHardwareId(hardwareId);
            if (device == null) {
                device = new Device(null, deviceType, null, hardwareId);
                deviceRepository.save(device);
                System.out.println("Registered device with hardwareId: " + hardwareId);
            } else {
                System.out.println("Device already registered: " + hardwareId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleNotificationMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            String[] parts = payload.split(",", 3); // hardwareId, title, message
            String hardwareId = parts[0];
            String title = parts[1];
            String notificationMessage = parts[2];

            Device device = deviceRepository.findByHardwareId(hardwareId);
            if (device != null && device.getName() != null) { // device is activated when it has a name
                Notification notification = new Notification(title, notificationMessage);
                notification.setEmitter(device);
                notification.setType(NotificationEventType.DOORLOCK_MISC); // TODO: set correct type once devices send them
                notificationRepository.save(notification);
                System.out.println("Saved notification for device: " + hardwareId);
            } else {
                System.out.println("Ignored notification from unregistered or inactive device: " + hardwareId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() throws MqttException {
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
        }
    }
}
