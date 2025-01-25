package ro.faur.apollo.home.domain.dto;

import ro.faur.apollo.device.domain.dtos.DeviceDTO;

import java.util.List;

public class HomeDTO {
    private String uuid;
    private String name;
    private String address;
    private List<DeviceDTO> devices;

    public HomeDTO(String uuid, String name, String address) {
        this.uuid = uuid;
        this.name = name;
        this.address = address;
    }

    public HomeDTO() {
    }

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
}
