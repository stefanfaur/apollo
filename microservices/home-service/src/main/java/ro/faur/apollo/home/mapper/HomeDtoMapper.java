package ro.faur.apollo.home.mapper;

import org.springframework.stereotype.Component;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.domain.HomeGuest;
import ro.faur.apollo.home.dto.DeviceDTO;
import ro.faur.apollo.home.dto.HomeDTO;
import ro.faur.apollo.home.dto.HomeGuestDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HomeDtoMapper {


    public HomeDtoMapper() {
    }

    /**
     * Convert Home entity to HomeDTO
     */
    public HomeDTO toDto(Home home) {
        if (home == null) {
            return null;
        }
        
        HomeDTO homeDTO = new HomeDTO();
        homeDTO.setUuid(home.getUuid());
        homeDTO.setName(home.getName());
        homeDTO.setAddress(home.getAddress());
        homeDTO.setDeviceUuids(home.getDeviceUuids());
        homeDTO.setAdminUuids(home.getAdminUuids());
        
        // Convert HomeGuest entities to HomeGuestDTOs
        List<HomeGuestDTO> guestDTOs = home.getGuests().stream()
                .map(this::homeGuestToDto)
                .collect(Collectors.toList());
        homeDTO.setGuests(guestDTOs);
        
        // Note: devices field will be populated by service layer via DeviceServiceClient
        return homeDTO;
    }

    /**
     * Convert Home entity to HomeDTO with devices
     */
    public HomeDTO toDto(Home home, List<DeviceDTO> devices) {
        HomeDTO homeDTO = toDto(home);
        if (homeDTO != null) {
            homeDTO.setDevices(devices);
        }
        return homeDTO;
    }

    /**
     * Convert HomeDTO to Home entity
     */
    public Home toEntity(HomeDTO homeDTO) {
        if (homeDTO == null) {
            return null;
        }
        
        Home home = new Home();
        home.setUuid(homeDTO.getUuid());
        home.setName(homeDTO.getName());
        home.setAddress(homeDTO.getAddress());
        
        if (homeDTO.getDeviceUuids() != null) {
            home.setDeviceUuids(homeDTO.getDeviceUuids());
        }
        if (homeDTO.getAdminUuids() != null) {
            home.setAdminUuids(homeDTO.getAdminUuids());
        }
        
        if (homeDTO.getGuests() != null) {
            List<HomeGuest> guests = homeDTO.getGuests().stream()
                    .map(this::homeGuestDtoToEntity)
                    .collect(Collectors.toList());
            home.setGuests(guests);
        }
        
        // Note: Device entities are not managed here
        return home;
    }

    /**
     * Convert list of HomeDTOs to list of Home entities
     */
    public List<Home> toEntity(List<HomeDTO> homeDTOs) {
        if (homeDTOs == null) {
            return null;
        }
        return homeDTOs.stream()
                .map(homeDTO -> toEntity(homeDTO))
                .collect(Collectors.toList());
    }

    /**
     * Convert HomeGuest entity to HomeGuestDTO
     * Updated to work with GuestDeviceRights instead of allowedDeviceUuids
     */
    private HomeGuestDTO homeGuestToDto(HomeGuest homeGuest) {
        if (homeGuest == null) {
            return null;
        }

        return new HomeGuestDTO(
                homeGuest.getUuid(),
                homeGuest.getUserUuid(),
                homeGuest.getHomeUuid()
        );
    }

    /**
     * Convert HomeGuestDTO to HomeGuest entity
     * Note: This creates a basic HomeGuest without device rights - 
     * device rights should be managed through HomeAccessService
     */
    private HomeGuest homeGuestDtoToEntity(HomeGuestDTO homeGuestDTO) {
        if (homeGuestDTO == null) {
            return null;
        }
        
        HomeGuest homeGuest = new HomeGuest();
        homeGuest.setUuid(homeGuestDTO.getUuid());
        homeGuest.setUserUuid(homeGuestDTO.getUserUuid());
        homeGuest.setHomeUuid(homeGuestDTO.getHomeUuid());
        
        // Note: Device rights are not set here as they should be managed 
        // through the proper HomeAccessService methods that handle validation
        // and proper GuestDeviceRights entity creation
        
        return homeGuest;
    }
} 