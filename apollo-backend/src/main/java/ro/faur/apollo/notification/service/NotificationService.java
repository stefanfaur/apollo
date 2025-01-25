package ro.faur.apollo.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.faur.apollo.libs.auth.utils.UserContext;
import ro.faur.apollo.notification.domain.Notification;
import ro.faur.apollo.notification.domain.dtos.NotificationDTO;
import ro.faur.apollo.notification.domain.dtos.NotificationDtoMapper;
import ro.faur.apollo.notification.repository.NotificationRepository;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserContext userContext;
    private final NotificationDtoMapper notificationDtoMapper;
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(NotificationRepository notificationRepository, UserContext userContext, NotificationDtoMapper notificationDtoMapper) {
        this.notificationRepository = notificationRepository;
        this.userContext = userContext;
        this.notificationDtoMapper = notificationDtoMapper;
    }

    /**
     * Get all notifications for a specific device by its UUID.
     * @return a list of notifications emitted by the device.
     */
    public List<NotificationDTO> getNotificationsForDevice(String deviceUuid) {
        return notificationRepository.findByDeviceUuid(deviceUuid).stream()
                .map(notificationDtoMapper::toDto)
                .toList();
    }

    /**
     * Get all notifications accessible to the user making the request.
     * @return a list of notifications from devices accessible to the user.
     */
    public List<NotificationDTO> getNotificationsForUser() {
        String userUuid = userContext.getUser().getUuid();
        List<Notification> notif = notificationRepository.findByUserUuid(userUuid);
        if (notif == null) {
            logger.warn("No notifications found for user with UUID {}", userUuid);
        }
        return notif.stream()
                .map(notificationDtoMapper::toDto)
                .toList();
    }
}
