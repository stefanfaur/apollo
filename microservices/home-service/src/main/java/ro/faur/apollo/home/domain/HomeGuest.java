package ro.faur.apollo.home.domain;

import jakarta.persistence.*;
import ro.faur.apollo.shared.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * A guest is a user that has access to certain devices in the home, but is not an admin.
 * He cannot add or remove devices, but can control the ones he has access to.
 */
@Entity
public class HomeGuest extends BaseEntity {

    @Column(name = "user_uuid", nullable = false)
    private String userUuid;

    @Column(name = "home_uuid", nullable = false)
    private String homeUuid;

    @OneToMany(mappedBy = "homeGuest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuestDeviceRights> deviceRights = new ArrayList<>();

    public HomeGuest() {
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getHomeUuid() {
        return homeUuid;
    }

    public void setHomeUuid(String homeUuid) {
        this.homeUuid = homeUuid;
    }

    public List<GuestDeviceRights> getDeviceRights() {
        return deviceRights;
    }

    public void setDeviceRights(List<GuestDeviceRights> deviceRights) {
        this.deviceRights = deviceRights;
    }
} 