package ro.faur.apollo.home.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ro.faur.apollo.device.domain.dtos.DeviceDTO;
import ro.faur.apollo.device.service.DeviceService;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.domain.dto.HomeDTO;
import ro.faur.apollo.home.domain.dto.HomeDtoMapper;
import ro.faur.apollo.home.repository.HomeRepository;
import ro.faur.apollo.libs.auth.user.domain.User;
import ro.faur.apollo.libs.auth.utils.UserContext;

import java.util.List;

@Service
public class HomeService {

    private final HomeRepository homeRepository;
    private final DeviceService deviceService;
    private final HomeDtoMapper homeDtoMapper;
    private final UserContext userContext;

    public HomeService(HomeRepository homeRepository, DeviceService deviceService, HomeDtoMapper homeDtoMapper, UserContext userContext) {
        this.homeRepository = homeRepository;
        this.deviceService = deviceService;
        this.homeDtoMapper = homeDtoMapper;
        this.userContext = userContext;
    }

    /**
     * Create an initial home, with the creator as sole admin.
     *
     * @param name
     * @param address
     */
    @Transactional
    public HomeDTO createHome(String name, String address) {
        Home home = new Home(name, address);
        home = homeRepository.save(home); // persist home so uuid can get added to admins join table
        home.getAdmins().add(userContext.getUser());
        home = homeRepository.save(home);
        return homeDtoMapper.toDto(home);
    }

    /**
     * Delete a home and unlink all devices linked to it.
     * @param homeUuid
     */
    @Transactional
    public boolean deleteHome(String homeUuid) {
        User user = userContext.getUser();
        if (!homeRepository.isUserAdminOfHome(user.getUuid(), homeUuid)) {
            throw new IllegalArgumentException("User is not an admin of the home.");
        }
        Home home = homeRepository.findById(homeUuid).orElse(null);
        if (home == null) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        deviceService.unlinkDevicesFromHome(home);
        homeRepository.delete(home);
        return true;
    }

    /**
     * Retrieves all Homes where the current user is either an admin or a guest.
     *
     * @return List of Homes accessible to the current user.
     */
    @Transactional
    public List<HomeDTO> getHomesForCurrentUser() {
        String userUuid = userContext.getUser().getUuid();
        if (userUuid == null) {
            throw new IllegalStateException("No authenticated user found.");
        }
        List<Home> homes = homeRepository.findByAdminOrGuest(userUuid);

        homes.forEach(home -> home.getDevices().size()); // force fetch of devices

        return homeDtoMapper.toDto(homes);
    }

    @Transactional
    public HomeDTO getHome(String homeUuid) {
        Home home = homeRepository.findById(homeUuid).orElse(null);
        if (home == null) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        home.getDevices().size(); // force fetch of devices

        return homeDtoMapper.toDto(home);
    }

    @Transactional
    public List<DeviceDTO> getHomeDevices(String homeUuid) {
        Home home = homeRepository.findById(homeUuid).orElse(null);
        if (home == null) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        return deviceService.getDevicesForHome(home);
    }

}
