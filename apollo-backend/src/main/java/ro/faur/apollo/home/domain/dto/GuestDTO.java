package ro.faur.apollo.home.domain.dto;

import ro.faur.apollo.device.domain.GuestDeviceRights;
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

    public static GuestDTO fromHomeGuest(HomeGuest homeGuest) {
        List<GuestDeviceRightsDTO> deviceRights = homeGuest.getDeviceRights().stream()
            .map(rights -> new GuestDeviceRightsDTO(
                rights.getDevice().getUuid(),
                rights.getAccessRights()
            ))
            .collect(Collectors.toList());

        return new GuestDTO(
            homeGuest.getUuid(),
            homeGuest.getUser().getEmail(),
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
