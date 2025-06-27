package ro.faur.apollo.home.domain;

import jakarta.persistence.*;
import ro.faur.apollo.shared.domain.BaseEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "home")
public class Home extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @ElementCollection
    @BatchSize(size = 256)
    @CollectionTable(name = "home_device_uuids", joinColumns = @JoinColumn(name = "home_uuid"))
    @Column(name = "device_uuid")
    private List<String> deviceUuids = new ArrayList<>();

    @ElementCollection
    @BatchSize(size = 256)
    @CollectionTable(name = "home_admin_uuids", joinColumns = @JoinColumn(name = "home_uuid"))
    @Column(name = "admin_uuid")
    private List<String> adminUuids = new ArrayList<>();

    @OneToMany(mappedBy = "homeUuid", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 256)
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

    public List<String> getDeviceUuids() {
        return deviceUuids;
    }

    public void setDeviceUuids(List<String> deviceUuids) {
        this.deviceUuids = deviceUuids;
    }

    public List<String> getAdminUuids() {
        return adminUuids;
    }

    public void setAdminUuids(List<String> adminUuids) {
        this.adminUuids = adminUuids;
    }

    public List<HomeGuest> getGuests() {
        return guests;
    }

    public void setGuests(List<HomeGuest> guests) {
        this.guests = guests;
    }
} 