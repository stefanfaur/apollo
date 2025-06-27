package ro.faur.apollo.home.service;

import org.springframework.stereotype.Service;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

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
    private final TransactionTemplate transactionTemplate;

    public HomeAccessService(
            HomeRepository homeRepository, 
            UserServiceClient userServiceClient,
            HomeGuestRepository homeGuestRepository,
            GuestDeviceRightsRepository guestDeviceRightsRepository,
            DeviceServiceClient deviceServiceClient,
            PlatformTransactionManager transactionManager
    ) {
        this.homeRepository = homeRepository;
        this.userServiceClient = userServiceClient;
        this.homeGuestRepository = homeGuestRepository;
        this.guestDeviceRightsRepository = guestDeviceRightsRepository;
        this.deviceServiceClient = deviceServiceClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public List<AdminDTO> getHomeAdmins(String homeUuid) {
        Home home = homeRepository.findById(homeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Home not found"));

        List<String> adminUuids = List.copyOf(home.getAdminUuids());
        List<UserDTO> users = userServiceClient.getUsersByUuids(adminUuids);
        return users.stream()
                .map(user -> AdminDTO.fromUser(user.getUuid(), user.getEmail()))
                .collect(Collectors.toList());
    }

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

        transactionTemplate.execute(status -> {
            Home homeTx = homeRepository.findById(homeUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Home not found"));
            homeTx.getAdminUuids().add(user.getUuid());
            homeRepository.save(homeTx);
            return null;
        });
    }

    public void removeHomeAdmin(String homeUuid, String adminUuid) {
        transactionTemplate.execute(status -> {
            Home homeTx = homeRepository.findById(homeUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Home not found"));
            homeTx.getAdminUuids().remove(adminUuid);
            homeRepository.save(homeTx);
            return null;
        });
    }

    public List<GuestDTO> getHomeGuests(String homeUuid) {
        return transactionTemplate.execute(status -> {
            Home homeTx = homeRepository.findById(homeUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Home not found"));

            List<String> guestUuids = homeTx.getGuests().stream()
                    .map(HomeGuest::getUserUuid)
                    .collect(Collectors.toList());

            List<UserDTO> users = userServiceClient.getUsersByUuids(guestUuids);
            Map<String, String> emailByUuid = users.stream()
                    .collect(Collectors.toMap(UserDTO::getUuid, UserDTO::getEmail));

            return homeTx.getGuests().stream()
                    .map(guest -> GuestDTO.fromHomeGuest(guest, emailByUuid.getOrDefault(guest.getUserUuid(), null)))
                    .collect(Collectors.toList());
        });
    }

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

        // Create device rights list (validation already passed)
        List<GuestDeviceRights> deviceRights = request.getDeviceRights().stream()
                .map(rightDto -> new GuestDeviceRights(guest, rightDto.getDeviceId(), rightDto.getRights()))
                .collect(Collectors.toList());

        guest.setDeviceRights(deviceRights);

        transactionTemplate.execute(status -> {
            Home homeTx = homeRepository.findById(homeUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Home not found"));
            homeTx.getGuests().add(guest);
            homeGuestRepository.save(guest);
            homeRepository.save(homeTx);
            return null;
        });
    }

    public void removeHomeGuest(String homeUuid, String guestUuid) {
        transactionTemplate.execute(status -> {
            Home homeTx = homeRepository.findById(homeUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Home not found"));

            HomeGuest guest = homeGuestRepository.findById(guestUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Guest not found"));

            if (!guest.getHomeUuid().equals(homeUuid)) {
                throw new IllegalArgumentException("Guest does not belong to this home");
            }

            homeTx.getGuests().remove(guest);
            homeRepository.save(homeTx);
            return null;
        });
    }

    public void updateGuestDeviceRights(String homeUuid, String guestUuid, List<GuestDeviceRightsDTO> deviceRights) {
        transactionTemplate.execute(status -> {
            Home homeTx = homeRepository.findById(homeUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Home not found"));

            HomeGuest guest = homeGuestRepository.findById(guestUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Guest not found"));

            if (!guest.getHomeUuid().equals(homeUuid)) {
                throw new IllegalArgumentException("Guest does not belong to this home");
            }

            // Clear existing device rights
            guest.getDeviceRights().clear();

            List<GuestDeviceRights> newRights = deviceRights.stream()
                    .map(rightDto -> new GuestDeviceRights(guest, rightDto.getDeviceId(), rightDto.getRights()))
                    .collect(Collectors.toList());

            guest.getDeviceRights().addAll(newRights);
            homeGuestRepository.save(guest);
            return null;
        });
    }
} 