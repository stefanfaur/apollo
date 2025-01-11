package ro.faur.apollo.notification.service;

import org.springframework.stereotype.Service;
import ro.faur.apollo.libs.auth.utils.UserContext;
import ro.faur.apollo.notification.domain.Notification;
import ro.faur.apollo.notification.repository.NotificationRepository;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserContext userContext;

    public NotificationService(NotificationRepository notificationRepository, UserContext userContext) {
        this.notificationRepository = notificationRepository;
        this.userContext = userContext;
    }

    /**
     * Get all notifications for a specific device by its UUID.
     * @return a list of notifications emitted by the device.
     */
    public List<Notification> getNotificationsForDevice(String deviceUuid) {
        return notificationRepository.findByDeviceUuid(deviceUuid);
    }

    /**
     * Get all notifications accessible to the user making the request.
     * @return a list of notifications from devices accessible to the user.
     */
    public List<Notification> getNotificationsForUser() {
        String userUuid = userContext.getUser().getUuid();
        return notificationRepository.findByUserUuid(userUuid);
    }
}
