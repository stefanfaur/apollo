package ro.faur.apollo.home.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.device.domain.GuestDeviceRights;
import ro.faur.apollo.device.repository.DeviceRepository;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.domain.HomeGuest;
import ro.faur.apollo.home.domain.dto.*;
import ro.faur.apollo.home.repository.HomeGuestRepository;
import ro.faur.apollo.home.repository.HomeRepository;
import ro.faur.apollo.libs.auth.user.domain.User;
import ro.faur.apollo.libs.auth.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HomeAccessService {

    private final HomeRepository homeRepository;
    private final UserRepository userRepository;
    private final HomeGuestRepository homeGuestRepository;
    private final DeviceRepository deviceRepository;

    public HomeAccessService(
            HomeRepository homeRepository,
            UserRepository userRepository,
            HomeGuestRepository homeGuestRepository,
            DeviceRepository deviceRepository
    ) {
        this.homeRepository = homeRepository;
        this.userRepository = userRepository;
        this.homeGuestRepository = homeGuestRepository;
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminDTO> getHomeAdmins(String homeUuid) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        return home.getAdmins().stream()
                .map(AdminDTO::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addHomeAdmin(String homeUuid, String email) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (home.getAdmins().contains(user)) {
            throw new IllegalArgumentException("User is already an admin of this home");
        }

        home.getAdmins().add(user);
        homeRepository.save(home);
    }

    @Transactional
    public void removeHomeAdmin(String homeUuid, String adminUuid) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        User admin = userRepository.findById(adminUuid)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        if (!home.getAdmins().contains(admin)) {
            throw new IllegalArgumentException("User is not an admin of this home");
        }

        if (home.getAdmins().size() == 1) {
            throw new IllegalArgumentException("Cannot remove the last admin");
        }

        home.getAdmins().remove(admin);
        homeRepository.save(home);
    }

    @Transactional(readOnly = true)
    public List<GuestDTO> getHomeGuests(String homeUuid) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        return home.getGuests().stream()
                .map(GuestDTO::fromHomeGuest)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addHomeGuest(String homeUuid, AddGuestRequestDTO request) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (home.getAdmins().contains(user)) {
            throw new IllegalArgumentException("User is already an admin of this home");
        }
        if (home.getGuests().stream().anyMatch(guest -> guest.getUser().equals(user))) {
            throw new IllegalArgumentException("User is already a guest of this home");
        }

        final HomeGuest guest = new HomeGuest();
        guest.setUser(user);
        guest.setHome(home);
        home.getGuests().add(guest);

        // Create device rights list before saving to ensure all devices exist
        List<GuestDeviceRights> deviceRights = request.getDeviceRights().stream()
                .map(rightDto -> {
                    Device device = deviceRepository.findById(rightDto.getDeviceId())
                            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + rightDto.getDeviceId()));

                    // Verify the device belongs to this home
                    if (!device.getHome().equals(home)) {
                        throw new IllegalArgumentException("Device " + rightDto.getDeviceId() + " does not belong to this home");
                    }

                    return new GuestDeviceRights(guest, device, rightDto.getRights());
                })
                .collect(Collectors.toList());

        // Set device rights before saving to ensure they're created in the same transaction
        guest.setDeviceRights(deviceRights);

        // Save everything in one transaction
        homeGuestRepository.save(guest);
        homeRepository.save(home);
    }


    @Transactional
    public void removeHomeGuest(String homeUuid, String guestUuid) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        HomeGuest guest = homeGuestRepository.findById(guestUuid)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found"));

        if (!guest.getHome().equals(home)) {
            throw new IllegalArgumentException("Guest does not belong to this home");
        }

        home.getGuests().remove(guest);
        homeRepository.save(home);
    }

    @Transactional
    public void updateGuestDeviceRights(String homeUuid, String guestUuid, List<GuestDeviceRightsDTO> deviceRights) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        HomeGuest guest = homeGuestRepository.findById(guestUuid)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found"));

        if (!guest.getHome().equals(home)) {
            throw new IllegalArgumentException("Guest does not belong to this home");
        }

        guest.getDeviceRights().clear();

        List<GuestDeviceRights> newRights = deviceRights.stream()
                .map(rightDto -> {
                    Device device = deviceRepository.findById(rightDto.getDeviceId())
                            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + rightDto.getDeviceId()));

                    return new GuestDeviceRights(guest, device, rightDto.getRights());
                })
                .collect(Collectors.toList());

        guest.getDeviceRights().addAll(newRights);
        homeGuestRepository.save(guest);
    }
}
