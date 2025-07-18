package ro.faur.apollo.notification.domain;

import jakarta.persistence.*;
import ro.faur.apollo.shared.domain.BaseEntity;

@Entity
@Table(indexes = {
        @Index(name = "idx_notification_device_uuid", columnList = "device_uuid")
})
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(value = EnumType.STRING)
    private NotificationEventType type;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "device_uuid")
    private String deviceUuid;

    public Notification(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public Notification() {
    }

    public Notification(String title, String message, NotificationEventType type, String mediaUrl, String deviceUuid) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.mediaUrl = mediaUrl;
        this.deviceUuid = deviceUuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationEventType getType() {
        return type;
    }

    public void setType(NotificationEventType type) {
        this.type = type;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }
} 