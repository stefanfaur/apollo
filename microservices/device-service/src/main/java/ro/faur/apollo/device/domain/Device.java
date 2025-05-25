package ro.faur.apollo.device.domain;

import jakarta.persistence.*;
import ro.faur.apollo.shared.domain.BaseEntity;
import ro.faur.apollo.shared.dto.DeviceStatus;

@Entity
public class Device extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "description")
    private String description;

    @Column(name = "hardware_id", nullable = false, unique = true)
    private String hardwareId;

    @Column(name = "home_uuid")
    private String homeUuid;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status = DeviceStatus.UNKNOWN;

    public Device(String name, String deviceType, String description, String hardwareId) {
        this.name = name;
        this.deviceType = deviceType;
        this.description = description;
        this.hardwareId = hardwareId;
    }

    public Device(String name, String deviceType, String description, String hardwareId, String homeUuid, DeviceStatus status) {
        this.name = name;
        this.deviceType = deviceType;
        this.description = description;
        this.hardwareId = hardwareId;
        this.homeUuid = homeUuid;
        this.status = status;
    }

    public Device() {
    }

    public String getHomeUuid() {
        return homeUuid;
    }

    public void setHomeUuid(String homeUuid) {
        this.homeUuid = homeUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }
} 