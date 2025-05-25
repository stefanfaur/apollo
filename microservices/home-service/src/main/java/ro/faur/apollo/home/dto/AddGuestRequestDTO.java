package ro.faur.apollo.home.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AddGuestRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotEmpty(message = "Device rights are required")
    private List<GuestDeviceRightsDTO> deviceRights;

    public AddGuestRequestDTO() {
    }

    public AddGuestRequestDTO(String email, List<GuestDeviceRightsDTO> deviceRights) {
        this.email = email;
        this.deviceRights = deviceRights;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<GuestDeviceRightsDTO> getDeviceRights() {
        return deviceRights;
    }

    public void setDeviceRights(List<GuestDeviceRightsDTO> deviceRights) {
        this.deviceRights = deviceRights;
    }
} 