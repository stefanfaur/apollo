package ro.faur.apollo.notification.dto.mqtt;

public class HelloMessage {
    private String hardwareId;
    private String deviceType;

    public String getHardwareId() {
        return hardwareId;
    }
    
    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
} 