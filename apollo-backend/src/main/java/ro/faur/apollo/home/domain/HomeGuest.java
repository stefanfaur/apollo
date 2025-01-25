package ro.faur.apollo.home.domain;

import jakarta.persistence.*;
import ro.faur.apollo.device.domain.GuestDeviceRights;
import ro.faur.apollo.libs.auth.user.domain.User;
import ro.faur.apollo.libs.persistence.domain.BaseEntity;

import java.util.List;

/**
 * A guest is an user that has access to certain devices in the home, but is not an admin.
 * He cannot add or remove devices, but can control the ones he has access to.
 */
@Entity
public class HomeGuest extends BaseEntity {

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "homeGuest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuestDeviceRights> deviceRights;

    @ManyToOne
    @JoinColumn(name = "home_uuid")
    private Home home;

    public HomeGuest() {
    }

    public HomeGuest(User user, List<GuestDeviceRights> deviceRights, Home home) {
        this.user = user;
        this.deviceRights = deviceRights;
        this.home = home;
    }

    public List<GuestDeviceRights> getDeviceRights() {
        return deviceRights;
    }

    public void setDeviceRights(List<GuestDeviceRights> deviceRights) {
        this.deviceRights = deviceRights;
    }

    public Home getHome() {
        return home;
    }

    public void setHome(Home home) {
        this.home = home;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
