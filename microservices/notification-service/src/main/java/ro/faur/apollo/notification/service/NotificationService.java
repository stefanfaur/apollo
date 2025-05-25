package ro.faur.apollo.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.faur.apollo.notification.client.HomeServiceClient;
import ro.faur.apollo.notification.domain.Notification;
import ro.faur.apollo.notification.dto.HomeDTO;
import ro.faur.apollo.notification.dto.NotificationDTO;
import ro.faur.apollo.notification.repository.NotificationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final HomeServiceClient homeServiceClient;
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(NotificationRepository notificationRepository, HomeServiceClient homeServiceClient) {
        this.notificationRepository = notificationRepository;
        this.homeServiceClient = homeServiceClient;
    }

    public List<NotificationDTO> getNotificationsForDevice(String deviceUuid) {
        return notificationRepository.findByDeviceUuid(deviceUuid).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getNotificationsForUser(String userUuid) {
        try {
            List<String> deviceUuids = getUserAccessibleDevices(userUuid);
            
            List<Notification> notifications = notificationRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .filter(notification -> deviceUuids.contains(notification.getDeviceUuid()))
                    .collect(Collectors.toList());
                    
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting notifications for user {}", userUuid, e);
            return List.of();
        }
    }

    public NotificationDTO saveNotification(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        return convertToDTO(saved);
    }

    private List<String> getUserAccessibleDevices(String userUuid) {
        try {
            List<HomeDTO> homes = homeServiceClient.getHomesForUser(userUuid);

            return homes.stream()
                    .map(HomeDTO::getDeviceUuids)
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting accessible devices for user {}", userUuid, e);
            return List.of();
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getUuid(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getMediaUrl(),
                notification.getCreatedAt(),
                notification.getDeviceUuid()
        );
    }
} 