package ro.faur.apollo.home.dto;

import java.util.List;

public class HomeDTO {
    private String uuid;
    private String name;
    private String address;
    private List<DeviceDTO> devices;
    private List<String> deviceUuids;
    private List<String> adminUuids;
    private List<HomeGuestDTO> guests;

    public HomeDTO() {}

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public List<DeviceDTO> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceDTO> devices) {
        this.devices = devices;
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

    public List<HomeGuestDTO> getGuests() {
        return guests;
    }

    public void setGuests(List<HomeGuestDTO> guests) {
        this.guests = guests;
    }
} 