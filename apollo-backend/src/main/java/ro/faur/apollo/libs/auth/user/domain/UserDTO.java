package ro.faur.apollo.libs.auth.user.domain;

public class UserDTO {

    private String username;
    private String email;

    public static UserDTO fromEntity(User user) {
        return new UserDTO(user.getUsername(), user.getEmail());
    }

    public UserDTO(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public UserDTO() {
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
}
