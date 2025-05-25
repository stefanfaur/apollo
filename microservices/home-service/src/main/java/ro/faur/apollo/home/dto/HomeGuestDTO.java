package ro.faur.apollo.home.dto;

public class HomeGuestDTO {
    private String uuid;
    private String userUuid;
    private String homeUuid;

    public HomeGuestDTO() {}

    public HomeGuestDTO(String uuid, String userUuid, String homeUuid) {
        this.uuid = uuid;
        this.userUuid = userUuid;
        this.homeUuid = homeUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getHomeUuid() {
        return homeUuid;
    }

    public void setHomeUuid(String homeUuid) {
        this.homeUuid = homeUuid;
    }

}