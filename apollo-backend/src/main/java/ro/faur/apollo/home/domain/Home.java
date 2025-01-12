package ro.faur.apollo.home.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.libs.auth.user.domain.User;
import ro.faur.apollo.libs.persistence.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "home")
public class Home extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "home", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Device> devices = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "home_admins",
            joinColumns = @JoinColumn(name = "home_uuid", referencedColumnName = "uuid"),
            inverseJoinColumns = @JoinColumn(name = "user_uuid", referencedColumnName = "uuid")
    )
    private List<User> admins = new ArrayList<>();

    @OneToMany(mappedBy = "home", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HomeGuest> guests = new ArrayList<>();

    public Home(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public Home() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public List<User> getAdmins() {
        return admins;
    }

    public void setAdmins(List<User> admins) {
        this.admins = admins;
    }

    public List<HomeGuest> getGuests() {
        return guests;
    }

    public void setGuests(List<HomeGuest> guests) {
        this.guests = guests;
    }
}
