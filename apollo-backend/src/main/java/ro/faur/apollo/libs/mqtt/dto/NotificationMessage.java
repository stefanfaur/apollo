package ro.faur.apollo.libs.mqtt.dto;

public class NotificationMessage {
    private String hardwareId;
    private String title;
    private String message;
    private String mediaUrl;

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
}
