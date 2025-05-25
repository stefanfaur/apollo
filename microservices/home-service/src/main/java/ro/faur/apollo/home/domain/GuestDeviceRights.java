package ro.faur.apollo.home.domain;

import jakarta.persistence.*;
import ro.faur.apollo.shared.domain.BaseEntity;

import java.util.Set;

/**
 * This entity represents the rights a guest of a home has on a device.
 * Adapted for microservices architecture using UUIDs instead of entity references.
 */
@Entity
public class GuestDeviceRights extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "home_guest_id", nullable = false)
    private HomeGuest homeGuest;

    @Column(name = "device_uuid", nullable = false)
    private String deviceUuid;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "guest_device_access_rights",
            joinColumns = @JoinColumn(name = "guest_device_rights_id")
    )
    @Column(name = "access_right")
    @Enumerated(EnumType.STRING)
    private Set<DeviceAccessRights> accessRights;

    public GuestDeviceRights(HomeGuest homeGuest, String deviceUuid, Set<DeviceAccessRights> accessRights) {
        this.homeGuest = homeGuest;
        this.deviceUuid = deviceUuid;
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

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public Set<DeviceAccessRights> getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(Set<DeviceAccessRights> accessRights) {
        this.accessRights = accessRights;
    }
} 