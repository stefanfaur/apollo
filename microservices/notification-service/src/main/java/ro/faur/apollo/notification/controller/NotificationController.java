package ro.faur.apollo.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.notification.dto.NotificationDTO;
import ro.faur.apollo.notification.service.NotificationService;
import ro.faur.apollo.shared.security.UserContext;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserContext userContext;

    public NotificationController(NotificationService notificationService, UserContext userContext) {
        this.notificationService = notificationService;
        this.userContext = userContext;
    }

    @GetMapping("/device/{deviceUuid}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForDevice(@PathVariable String deviceUuid) {
        List<NotificationDTO> notifications = notificationService.getNotificationsForDevice(deviceUuid);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForUser() {
        String userUuid = userContext.getCurrentUserUuid();
        List<NotificationDTO> notifications = notificationService.getNotificationsForUser(userUuid);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userUuid}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForUser(@PathVariable String userUuid) {
        List<NotificationDTO> notifications = notificationService.getNotificationsForUser(userUuid);
        return ResponseEntity.ok(notifications);
    }
} 