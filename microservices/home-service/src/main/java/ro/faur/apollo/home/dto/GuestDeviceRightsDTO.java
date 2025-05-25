package ro.faur.apollo.home.dto;

import ro.faur.apollo.home.domain.DeviceAccessRights;

import java.util.Set;

public class GuestDeviceRightsDTO {
    private String deviceId;
    private Set<DeviceAccessRights> rights;

    public GuestDeviceRightsDTO() {
    }

    public GuestDeviceRightsDTO(String deviceId, Set<DeviceAccessRights> rights) {
        this.deviceId = deviceId;
        this.rights = rights;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Set<DeviceAccessRights> getRights() {
        return rights;
    }

    public void setRights(Set<DeviceAccessRights> rights) {
        this.rights = rights;
    }
} 