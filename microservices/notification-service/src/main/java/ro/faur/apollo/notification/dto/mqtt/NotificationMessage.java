package ro.faur.apollo.notification.dto.mqtt;

public class NotificationMessage {
    private String hardwareId;
    private String title;
    private String message;
    private String mediaUrl;
    private String eventType;

    public String getHardwareId() {
        return hardwareId;
    }
    
    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
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
    
    public String getMediaUrl() {
        return mediaUrl;
    }
    
    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
} 