package ro.faur.apollo.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.faur.apollo.notification.domain.Notification;
import ro.faur.apollo.notification.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Endpoint to get all notifications for a specific device.
     *
     * @return a list of notifications.
     */
    @GetMapping("/device/{deviceUuid}")
    public ResponseEntity<List<Notification>> getNotificationsForDevice(@PathVariable String deviceUuid) {
        List<Notification> notifications = notificationService.getNotificationsForDevice(deviceUuid);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Endpoint to get all notifications for a specific user.
     *
     * @return a list of notifications.
     */
    @GetMapping("/user")
    public ResponseEntity<List<Notification>> getNotificationsForUser() {
        List<Notification> notifications = notificationService.getNotificationsForUser();
        return ResponseEntity.ok(notifications);
    }

}
