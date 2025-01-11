package ro.faur.apollo.home.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.domain.dto.HomeDTO;
import ro.faur.apollo.home.repository.HomeRepository;
import ro.faur.apollo.libs.auth.utils.UserContext;

import java.util.List;

@Service
public class HomeService {

    private final HomeRepository homeRepository;
    private final UserContext userContext;

    public HomeService(HomeRepository homeRepository, UserContext userContext) {
        this.homeRepository = homeRepository;
        this.userContext = userContext;
    }

    /**
     * Create an initial home, with the creator as sole admin.
     * @param name
     * @param address
     */
    @Transactional
    public HomeDTO createHome(String name, String address) {
        Home home = new Home(name, address);
        home = homeRepository.save(home); // persist home so uuid can get added to admins join table
        home.getAdmins().add(userContext.getUser());
        home = homeRepository.save(home);
        return HomeDTO.from(home);
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
        List<Home> homes =  homeRepository.findByAdminOrGuest(userUuid);

        homes.forEach(home -> home.getDevices().size()); // force fetch of devices

        return HomeDTO.from(homes);
    }

    public HomeDTO getHome(String homeUuid) {
        return HomeDTO.from(homeRepository.findById(homeUuid).orElse(null));
    }

}
