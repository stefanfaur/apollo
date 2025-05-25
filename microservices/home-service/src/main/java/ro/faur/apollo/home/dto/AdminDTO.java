package ro.faur.apollo.home.dto;

public class AdminDTO {
    private String uuid;
    private String email;

    public AdminDTO() {
    }

    public AdminDTO(String uuid, String email) {
        this.uuid = uuid;
        this.email = email;
    }

    public static AdminDTO fromUser(String uuid, String email) {
        return new AdminDTO(uuid, email);
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
} 