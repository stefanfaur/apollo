package ro.faur.apollo.home.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateDeviceRequestDTO {
    @NotBlank(message = "Name is required")
    private String name;

    private String deviceType;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Hardware ID is required")
    private String hardwareId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }
} 