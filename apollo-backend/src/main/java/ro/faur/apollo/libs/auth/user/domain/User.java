package ro.faur.apollo.libs.auth.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import ro.faur.apollo.libs.persistence.BaseEntity;


@Entity(name = "users")
public class User extends BaseEntity {

    @Column(name = "username",unique = true)
    private String username;
    @Column(name = "email",unique = true)
    private String email;
    @Column(name = "password")
    private String password;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "roles")
    private String roles;

    public User(String username, String email, String password, String googleId, String roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.googleId = googleId;
        this.roles = roles;
    }

    public User() {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
