package ro.faur.apollo.home.service;

import org.springframework.stereotype.Service;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.domain.dto.HomeDTO;
import ro.faur.apollo.home.domain.dto.HomeDtoMapper;
import ro.faur.apollo.home.repository.HomeRepository;
import ro.faur.apollo.libs.auth.user.domain.User;
import ro.faur.apollo.libs.auth.user.service.UserService;

@Service
public class HomeAccessService {

    private final HomeRepository homeRepository;
    private final UserService userService;
    private final HomeDtoMapper homeDtoMapper;

    public HomeAccessService(HomeRepository homeRepository, UserService userService, HomeDtoMapper homeDtoMapper) {
        this.homeRepository = homeRepository;
        this.userService = userService;
        this.homeDtoMapper = homeDtoMapper;
    }

    public boolean isUserAdminOfHome(String userUuid, String homeUuid) {
        return homeRepository.isUserAdminOfHome(userUuid, homeUuid);
    }

    public boolean isUserGuestOfHome(String userUuid, String homeUuid) {
        return homeRepository.isUserGuestOfHome(userUuid, homeUuid);
    }

    public HomeDTO addAdminToHome(String username, String homeUuid) {
        Home home = homeRepository.findByUuid(homeUuid);
        if (home == null) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found for username: " + username);
        }
        home.getAdmins().add(user);
        home = homeRepository.save(home);

        return homeDtoMapper.toDto(home);
    }

    public HomeDTO removeAdminFromHome(String username, String homeUuid) {
        Home home = homeRepository.findByUuid(homeUuid);
        if (home == null) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found for username: " + username);
        }
        home.getAdmins().remove(user);
        home = homeRepository.save(home);

        return homeDtoMapper.toDto(home);
    }
}
