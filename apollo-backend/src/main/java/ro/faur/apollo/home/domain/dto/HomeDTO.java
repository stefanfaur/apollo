package ro.faur.apollo.home.domain.dto;

import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.home.domain.Home;

import java.util.List;
import java.util.stream.Collectors;

public class HomeDTO {
    private String uuid;
    private String name;
    private String address;
    private List<Device> devices;

    public HomeDTO(String uuid, String name, String address) {
        this.uuid = uuid;
        this.name = name;
        this.address = address;
    }

    public HomeDTO() {
    }

    // Getters and Setters

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

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    /**
     * Convert a Home entity to a HomeDTO.
     *
     * @param home the Home entity.
     * @return a HomeDTO instance.
     */
    public static HomeDTO from(Home home) {
        HomeDTO homeDTO = new HomeDTO(home.getUuid(), home.getName(), home.getAddress());
        homeDTO.setDevices(home.getDevices()); // Assuming devices are directly serializable
        return homeDTO;
    }

    /**
     * Convert a list of Home entities to a list of HomeDTOs.
     *
     * @param homes the list of Home entities.
     * @return a list of HomeDTOs.
     */
    public static List<HomeDTO> from(List<Home> homes) {
        return homes.stream().map(HomeDTO::from).collect(Collectors.toList());
    }
}
