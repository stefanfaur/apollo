package ro.faur.apollo.device.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.libs.persistence.domain.BaseEntity;

@Entity
public class Device extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Derived from the hardware id
     */
    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "description")
    private String description;

    @Column(name = "hardware_id", nullable = false)
    private String hardwareId;

    @ManyToOne
    @JoinColumn(name = "home_uuid")
    @JsonIgnore // TODO: remove this after no need when we use DTOs
    private Home home;

    public Device(String name, String deviceType, String description, String hardwareId) {
        this.name = name;
        this.deviceType = deviceType;
        this.description = description;
        this.hardwareId = hardwareId;
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
}
