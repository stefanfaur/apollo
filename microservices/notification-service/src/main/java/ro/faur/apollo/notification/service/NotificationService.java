package ro.faur.apollo.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.faur.apollo.notification.client.HomeServiceClient;
import ro.faur.apollo.notification.domain.Notification;
import ro.faur.apollo.notification.dto.NotificationDTO;
import ro.faur.apollo.notification.repository.NotificationRepository;
import ro.faur.apollo.shared.dto.HomeSummaryDTO;

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

            // If user has no accessible devices, return empty list to
            // avoid an unnecessary database call.
            if (deviceUuids.isEmpty()) {
                return List.of();
            }

            List<Notification> notifications = notificationRepository
                    .findByDeviceUuidInOrderByCreatedAtDesc(deviceUuids);

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
            List<HomeSummaryDTO> summaries = homeServiceClient.getHomeSummariesForUser(userUuid);

            return summaries.stream()
                    .map(HomeSummaryDTO::getDeviceUuids)
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