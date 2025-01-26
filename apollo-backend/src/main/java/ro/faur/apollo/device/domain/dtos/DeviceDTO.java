package ro.faur.apollo.device.domain.dtos;

import ro.faur.apollo.device.domain.DeviceStatus;

public class DeviceDTO {

    private String uuid;
    private String name;
    private String deviceType;
    private String description;
    private String hardwareId;
    private DeviceStatus status;
    private String homeUuid;

    public DeviceDTO(String uuid, String name, String deviceType, String description, String hardwareId, DeviceStatus status, String homeUuid) {
        this.uuid = uuid;
        this.name = name;
        this.deviceType = deviceType;
        this.description = description;
        this.hardwareId = hardwareId;
        this.status = status;
        this.homeUuid = homeUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public String getHomeUuid() {
        return homeUuid;
    }

    public void setHomeUuid(String homeUuid) {
        this.homeUuid = homeUuid;
    }
}
