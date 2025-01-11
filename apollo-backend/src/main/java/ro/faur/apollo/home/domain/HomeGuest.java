package ro.faur.apollo.home.domain;

import jakarta.persistence.*;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.libs.auth.user.domain.User;
import ro.faur.apollo.libs.persistence.BaseEntity;

import java.util.List;

/**
 * A guest is an user that has access to certain devices in the home, but is not an admin.
 * He cannot add or remove devices, but can control the ones he has access to.
 */
@Entity
public class HomeGuest extends BaseEntity {

    @ManyToOne
    private User user;

    @ManyToMany
    @JoinTable(
            name = "home_guest_permitted_devices",
            joinColumns = @JoinColumn(name = "home_guest_id"),
            inverseJoinColumns = @JoinColumn(name = "device_id")
    )
    private List<Device> permittedDevices;

    @ManyToOne
    @JoinColumn(name = "home_uuid")
    private Home home;

    public HomeGuest(User user, List<Device> permittedDevices, Home home) {
        this.user = user;
        this.permittedDevices = permittedDevices;
        this.home = home;
    }

    public HomeGuest() {
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

    public List<Device> getPermittedDevices() {
        return permittedDevices;
    }

    public void setPermittedDevices(List<Device> permittedDevices) {
        this.permittedDevices = permittedDevices;
    }
}
