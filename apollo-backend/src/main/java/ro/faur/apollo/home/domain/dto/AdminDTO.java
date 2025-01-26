package ro.faur.apollo.home.domain.dto;

import ro.faur.apollo.libs.auth.user.domain.User;

public class AdminDTO {
    private String uuid;
    private String email;

    public AdminDTO() {
    }

    public AdminDTO(String uuid, String email) {
        this.uuid = uuid;
        this.email = email;
    }

    public static AdminDTO fromUser(User user) {
        return new AdminDTO(
            user.getUuid(),
            user.getEmail()
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
}
