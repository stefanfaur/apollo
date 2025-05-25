package ro.faur.apollo.home.dto;

import ro.faur.apollo.home.domain.HomeGuest;

import java.util.List;
import java.util.stream.Collectors;

public class GuestDTO {
    private String uuid;
    private String email;
    private List<GuestDeviceRightsDTO> devices;

    public GuestDTO() {
    }

    public GuestDTO(String uuid, String email, List<GuestDeviceRightsDTO> devices) {
        this.uuid = uuid;
        this.email = email;
        this.devices = devices;
    }

    public static GuestDTO fromHomeGuest(HomeGuest homeGuest, String email) {
        List<GuestDeviceRightsDTO> deviceRights = homeGuest.getDeviceRights().stream()
            .map(rights -> new GuestDeviceRightsDTO(
                rights.getDeviceUuid(),
                rights.getAccessRights()
            ))
            .collect(Collectors.toList());

        return new GuestDTO(
            homeGuest.getUuid(),
            email,
            deviceRights
        );
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<GuestDeviceRightsDTO> getDevices() {
        return devices;
    }

    public void setDevices(List<GuestDeviceRightsDTO> devices) {
        this.devices = devices;
    }
} 