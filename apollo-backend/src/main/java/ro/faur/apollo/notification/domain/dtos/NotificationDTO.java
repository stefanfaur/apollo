package ro.faur.apollo.notification.domain.dtos;

import ro.faur.apollo.device.domain.dtos.DeviceDTO;
import ro.faur.apollo.notification.domain.types.NotificationEventType;

import java.time.LocalDateTime;

public class NotificationDTO {
    private String uuid;
    private String title;
    private String message;
    private NotificationEventType type;
    private String mediaUrl;
    private LocalDateTime createdAt;
    private DeviceDTO emitter;

    public NotificationDTO(String uuid, String title, String message, NotificationEventType type, String mediaUrl, LocalDateTime createdAt, DeviceDTO emitter) {
        this.uuid = uuid;
        this.title = title;
        this.message = message;
        this.type = type;
        this.mediaUrl = mediaUrl;
        this.createdAt = createdAt;
        this.emitter = emitter;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DeviceDTO getEmitter() {
        return emitter;
    }

    public void setEmitter(DeviceDTO emitter) {
        this.emitter = emitter;
    }
}
