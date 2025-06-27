package ro.faur.apollo.home.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.faur.apollo.home.client.DeviceServiceClient;
import ro.faur.apollo.home.client.UserServiceClient;
import ro.faur.apollo.home.domain.GuestDeviceRights;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.domain.HomeGuest;
import ro.faur.apollo.home.dto.*;
import ro.faur.apollo.home.repository.GuestDeviceRightsRepository;
import ro.faur.apollo.home.repository.HomeGuestRepository;
import ro.faur.apollo.home.repository.HomeRepository;
import ro.faur.apollo.shared.dto.UserDTO;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class HomeAccessService {

    private final HomeRepository homeRepository;
    private final UserServiceClient userServiceClient;
    private final HomeGuestRepository homeGuestRepository;
    private final GuestDeviceRightsRepository guestDeviceRightsRepository;
    private final DeviceServiceClient deviceServiceClient;

    public HomeAccessService(
            HomeRepository homeRepository, 
            UserServiceClient userServiceClient,
            HomeGuestRepository homeGuestRepository,
            GuestDeviceRightsRepository guestDeviceRightsRepository,
            DeviceServiceClient deviceServiceClient
    ) {
        this.homeRepository = homeRepository;
        this.userServiceClient = userServiceClient;
        this.homeGuestRepository = homeGuestRepository;
        this.guestDeviceRightsRepository = guestDeviceRightsRepository;
        this.deviceServiceClient = deviceServiceClient;
    }

    @Transactional(readOnly = true)
    public List<AdminDTO> getHomeAdmins(String homeUuid) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        List<String> adminUuids = home.getAdminUuids();
        List<UserDTO> users = userServiceClient.getUsersByUuids(adminUuids);
        return users.stream()
                .map(user -> AdminDTO.fromUser(user.getUuid(), user.getEmail()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addHomeAdmin(String homeUuid, String email) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        UserDTO user;
        try {
            user = userServiceClient.getUserByEmail(email);
        } catch (Exception e) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }

        if (home.getAdminUuids().contains(user.getUuid())) {
            throw new IllegalArgumentException("User is already an admin of this home");
        }

        home.getAdminUuids().add(user.getUuid());
        homeRepository.save(home);
    }

    @Transactional
    public void removeHomeAdmin(String homeUuid, String adminUuid) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        if (!home.getAdminUuids().contains(adminUuid)) {
            throw new IllegalArgumentException("User is not an admin of this home");
        }

        if (home.getAdminUuids().size() == 1) {
            throw new IllegalArgumentException("Cannot remove the last admin");
        }

        home.getAdminUuids().remove(adminUuid);
        homeRepository.save(home);
    }

    @Transactional(readOnly = true)
    public List<GuestDTO> getHomeGuests(String homeUuid) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        List<String> guestUuids = home.getGuests().stream()
                .map(HomeGuest::getUserUuid)
                .collect(Collectors.toList());

        List<UserDTO> users = userServiceClient.getUsersByUuids(guestUuids);
        Map<String, String> emailByUuid = users.stream().collect(Collectors.toMap(UserDTO::getUuid, UserDTO::getEmail));

        return home.getGuests().stream()
                .map(guest -> GuestDTO.fromHomeGuest(guest, emailByUuid.getOrDefault(guest.getUserUuid(), null)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addHomeGuest(String homeUuid, AddGuestRequestDTO request) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        UserDTO user;
        try {
            user = userServiceClient.getUserByEmail(request.getEmail());
        } catch (Exception e) {
            throw new IllegalArgumentException("User not found with email: " + request.getEmail());
        }

        if (home.getAdminUuids().contains(user.getUuid())) {
            throw new IllegalArgumentException("User is already an admin of this home");
        }

        if (home.getGuests().stream().anyMatch(guest -> guest.getUserUuid().equals(user.getUuid()))) {
            throw new IllegalArgumentException("User is already a guest of this home");
        }

        final HomeGuest guest = new HomeGuest();
        guest.setUserUuid(user.getUuid());
        guest.setHomeUuid(homeUuid);
        home.getGuests().add(guest);

        // Create device rights list before saving to ensure all devices exist
        List<GuestDeviceRights> deviceRights = request.getDeviceRights().stream()
                .map(rightDto -> {
                    // Verify the device belongs to this home
                    if (!home.getDeviceUuids().contains(rightDto.getDeviceId())) {
                        throw new IllegalArgumentException("Device " + rightDto.getDeviceId() + " does not belong to this home");
                    }

                    // Verify device exists in device service
                    try {
                        deviceServiceClient.getDevice(rightDto.getDeviceId());
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Device not found: " + rightDto.getDeviceId());
                    }

                    return new GuestDeviceRights(guest, rightDto.getDeviceId(), rightDto.getRights());
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

        if (!guest.getHomeUuid().equals(homeUuid)) {
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

        if (!guest.getHomeUuid().equals(homeUuid)) {
            throw new IllegalArgumentException("Guest does not belong to this home");
        }

        // Clear existing device rights
        guest.getDeviceRights().clear();

        // Create new device rights
        List<GuestDeviceRights> newRights = deviceRights.stream()
                .map(rightDto -> {
                    // Verify the device belongs to this home
                    if (!home.getDeviceUuids().contains(rightDto.getDeviceId())) {
                        throw new IllegalArgumentException("Device " + rightDto.getDeviceId() + " does not belong to this home");
                    }

                    // Verify device exists in device service
                    try {
                        deviceServiceClient.getDevice(rightDto.getDeviceId());
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Device not found: " + rightDto.getDeviceId());
                    }

                    return new GuestDeviceRights(guest, rightDto.getDeviceId(), rightDto.getRights());
                })
                .collect(Collectors.toList());

        // Add new device rights
        guest.getDeviceRights().addAll(newRights);
        homeGuestRepository.save(guest);
    }
} 