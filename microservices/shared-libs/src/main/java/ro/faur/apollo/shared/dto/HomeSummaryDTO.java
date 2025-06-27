package ro.faur.apollo.shared.dto;

import java.util.List;

public class HomeSummaryDTO {
    private String uuid;
    private List<String> deviceUuids;

    public HomeSummaryDTO() {}

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public List<String> getDeviceUuids() { return deviceUuids; }
    public void setDeviceUuids(List<String> deviceUuids) { this.deviceUuids = deviceUuids; }
} 