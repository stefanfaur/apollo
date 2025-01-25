package ro.faur.apollo.device.domain;

import jakarta.persistence.*;
import ro.faur.apollo.home.domain.HomeGuest;
import ro.faur.apollo.libs.persistence.domain.BaseEntity;

import java.util.Set;

/**
 * This entity represents the rights a guest of a home has on a device.
 */
@Entity
public class GuestDeviceRights extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "home_guest_id", nullable = false)
    private HomeGuest homeGuest;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "guest_device_access_rights",
            joinColumns = @JoinColumn(name = "guest_device_rights_id")
    )
    @Column(name = "access_right")
    @Enumerated(EnumType.STRING)
    private Set<DeviceAccessRights> accessRights;

    public GuestDeviceRights(HomeGuest homeGuest, Device device, Set<DeviceAccessRights> accessRights) {
        this.homeGuest = homeGuest;
        this.device = device;
        this.accessRights = accessRights;
    }

    public GuestDeviceRights() {
    }

    public HomeGuest getHomeGuest() {
        return homeGuest;
    }

    public void setHomeGuest(HomeGuest homeGuest) {
        this.homeGuest = homeGuest;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Set<DeviceAccessRights> getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(Set<DeviceAccessRights> accessRights) {
        this.accessRights = accessRights;
    }
}
