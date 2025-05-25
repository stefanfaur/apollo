package ro.faur.apollo.shared.dto;

public class UserDTO {
    private String uuid;
    private String username;
    private String email;
    private String roles;

    public UserDTO() {}

    public UserDTO(String uuid, String username, String email, String roles) {
        this.uuid = uuid;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
} 