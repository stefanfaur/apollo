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

import org.springframework.cache.annotation.CacheEvict;

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
            List<String> deviceUuids = getUserAccessibleDevicesInternal(userUuid);

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

    /**
     * Internal method to get user accessible devices without caching.
     * This allows the Feign circuit breaker and fallback to work properly.
     */
    private List<String> getUserAccessibleDevicesInternal(String userUuid) {
        try {
            List<HomeSummaryDTO> summaries = homeServiceClient.getHomeSummariesForUser(userUuid);

            return summaries.stream()
                    .map(HomeSummaryDTO::getDeviceUuids)
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Log connection failures at DEBUG level to reduce noise, since this is handled gracefully
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                logger.debug("Home service unavailable for user {}, falling back to empty list", userUuid);
            } else {
                logger.warn("Failed to get accessible devices for user {}, falling back to empty list: {}", userUuid, e.getMessage());
            }
            // Return empty list as fallback - this matches the behavior of HomeServiceClientFallback
            return List.of();
        }
    }

    /**
     * Public method for external access to user accessible devices.
     * This can be called directly when caching is not needed.
     */
    public List<String> getUserAccessibleDevices(String userUuid) {
        return getUserAccessibleDevicesInternal(userUuid);
    }

    /**
     * Evicts the user notifications cache for a specific user.
     * This should be called when a user's device access changes.
     */
    @CacheEvict(value = "userNotifications", key = "#userUuid")
    public void evictUserNotificationsCache(String userUuid) {
        logger.debug("Evicted userNotifications cache for user: {}", userUuid);
    }

    /**
     * Evicts the user accessible devices cache for a specific user.
     * This should be called when a user's device access changes (e.g., devices added/removed from homes).
     */
    @CacheEvict(value = "userAccessibleDevices", key = "#userUuid")
    public void evictUserAccessibleDevicesCache(String userUuid) {
        logger.debug("Evicted userAccessibleDevices cache for user: {}", userUuid);
    }

    /**
     * Evicts the entire user accessible devices cache.
     * This should be called when global device access changes that affect multiple users.
     */
    @CacheEvict(value = "userAccessibleDevices", allEntries = true)
    public void evictAllUserAccessibleDevicesCache() {
        logger.debug("Evicted all userAccessibleDevices cache entries");
    }

    /**
     * Evicts all notification-related caches.
     * This should be called when global changes affect multiple users.
     */
    @CacheEvict(value = {"userNotifications", "userAccessibleDevices"}, allEntries = true)
    public void evictAllCaches() {
        logger.debug("Evicted all notification-related caches");
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