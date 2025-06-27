package ro.faur.apollo.home.dto;

import java.util.List;

public class HomeSummaryDTO {
    private String uuid;
    private List<String> deviceUuids;

    public HomeSummaryDTO() {}

    public HomeSummaryDTO(String uuid, List<String> deviceUuids) {
        this.uuid = uuid;
        this.deviceUuids = deviceUuids;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<String> getDeviceUuids() {
        return deviceUuids;
    }

    public void setDeviceUuids(List<String> deviceUuids) {
        this.deviceUuids = deviceUuids;
    }
} 