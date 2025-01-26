package ro.faur.apollo.device.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.libs.persistence.domain.BaseEntity;

@Entity
public class Device extends BaseEntity {

    @Column(name = "name")
    private String name;

    /**
     * received from device in hello message
     */
    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "description")
    private String description;

    /**
     * received from device in hello message
     */
    @Column(name = "hardware_id", nullable = false, unique = true)
    private String hardwareId;

    @ManyToOne
    @JoinColumn(name = "home_uuid")
    @JsonIgnore // TODO: remove this after no need when we use DTOs
    private Home home;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status = DeviceStatus.UNKNOWN;

    public Device(String name, String deviceType, String description, String hardwareId) {
        this.name = name;
        this.deviceType = deviceType;
        this.description = description;
        this.hardwareId = hardwareId;
    }

    public Device(String name, String deviceType, String description, String hardwareId, Home home, DeviceStatus status) {
        this.name = name;
        this.deviceType = deviceType;
        this.description = description;
        this.hardwareId = hardwareId;
        this.home = home;
        this.status = status;
    }

    public Device() {
    }

    public Home getHome() {
        return home;
    }

    public void setHome(Home home) {
        this.home = home;
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
