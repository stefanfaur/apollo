package ro.faur.apollo.home.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.home.dto.*;
import ro.faur.apollo.home.service.HomeService;
import ro.faur.apollo.home.service.HomeAccessService;
import ro.faur.apollo.shared.security.UserContext;
import ro.faur.apollo.shared.exception.DeviceException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final HomeService homeService;
    private final HomeAccessService homeAccessService;
    private final UserContext userContext;

    public HomeController(HomeService homeService, HomeAccessService homeAccessService, UserContext userContext) {
        this.homeService = homeService;
        this.homeAccessService = homeAccessService;
        this.userContext = userContext;
    }

    @PostMapping()
    public ResponseEntity<HomeDTO> createHome(@RequestParam String name, @RequestParam String address) {
        String currentUserUuid = userContext.getCurrentUserUuid();
        return ResponseEntity.ok(homeService.createHome(name, address, currentUserUuid));
    }

    @GetMapping
    public ResponseEntity<List<HomeDTO>> getHomesForCurrentUser() {
        String currentUserUuid = userContext.getCurrentUserUuid();
        return ResponseEntity.ok(homeService.getHomesForUser(currentUserUuid));
    }

    @GetMapping("/user/{userUuid}")
    public ResponseEntity<List<HomeDTO>> getHomesForUser(@PathVariable String userUuid) {
        List<HomeDTO> homes = homeService.getHomesForUser(userUuid);
        return ResponseEntity.ok(homes);
    }

    @GetMapping("/{homeUuid}")
    public ResponseEntity<HomeDTO> getHome(@PathVariable String homeUuid) {
        try {
            HomeDTO home = homeService.getHome(homeUuid);
            return ResponseEntity.ok(home);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{homeUuid}")
    public ResponseEntity<?> deleteHome(@PathVariable String homeUuid) {
        try {
            String currentUserUuid = userContext.getCurrentUserUuid();
            boolean success = homeService.deleteHome(homeUuid, currentUserUuid);
            return ResponseEntity.ok(success);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{homeUuid}/admins")
    public ResponseEntity<List<AdminDTO>> getHomeAdmins(@PathVariable String homeUuid) {
        try {
            List<AdminDTO> admins = homeAccessService.getHomeAdmins(homeUuid);
            return ResponseEntity.ok(admins);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{homeUuid}/admins")
    public ResponseEntity<?> addHomeAdmin(@PathVariable String homeUuid, @RequestParam String email) {
        try {
            homeAccessService.addHomeAdmin(homeUuid, email);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{homeUuid}/admins/{adminUuid}")
    public ResponseEntity<?> removeHomeAdmin(@PathVariable String homeUuid, @PathVariable String adminUuid) {
        try {
            homeAccessService.removeHomeAdmin(homeUuid, adminUuid);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{homeUuid}/guests")
    public ResponseEntity<List<GuestDTO>> getHomeGuests(@PathVariable String homeUuid) {
        try {
            List<GuestDTO> guests = homeAccessService.getHomeGuests(homeUuid);
            return ResponseEntity.ok(guests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{homeUuid}/guests")
    public ResponseEntity<?> addHomeGuest(
            @PathVariable String homeUuid,
            @Valid @RequestBody AddGuestRequestDTO request) {
        try {
            homeAccessService.addHomeGuest(homeUuid, request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to add guest to home", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{homeUuid}/guests/{guestUuid}")
    public ResponseEntity<?> removeHomeGuest(@PathVariable String homeUuid, @PathVariable String guestUuid) {
        try {
            homeAccessService.removeHomeGuest(homeUuid, guestUuid);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{homeUuid}/guests/{guestUuid}/devices")
    public ResponseEntity<?> updateGuestDeviceRights(
            @PathVariable String homeUuid,
            @PathVariable String guestUuid,
            @Valid @RequestBody List<GuestDeviceRightsDTO> deviceRights) {
        try {
            homeAccessService.updateGuestDeviceRights(homeUuid, guestUuid, deviceRights);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{homeUuid}/devices")
    public ResponseEntity<List<DeviceDTO>> getHomeDevices(@PathVariable String homeUuid) {
        try {
            List<DeviceDTO> devices = homeService.getHomeDevices(homeUuid);
            return ResponseEntity.ok(devices);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{homeUuid}/devices")
    public ResponseEntity<?> addDeviceToHome(
            @PathVariable String homeUuid,
            @RequestBody CreateDeviceRequestDTO request) {
        try {
            DeviceDTO device = homeService.createDeviceInHome(
                    homeUuid,
                    request.getName(),
                    request.getDeviceType(),
                    request.getDescription(),
                    request.getHardwareId());
            return ResponseEntity.ok(device);
        } catch (DeviceException.DeviceNotRegisteredException e) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "DEVICE_NOT_REGISTERED",
                "message", e.getMessage()
            ));
        } catch (DeviceException.DeviceAlreadyLinkedException e) {
            return ResponseEntity.status(409).body(Map.of(
                "error", "DEVICE_ALREADY_LINKED", 
                "message", e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_REQUEST",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{homeUuid}/devices/{deviceUuid}")
    public ResponseEntity<?> unlinkDeviceFromHome(
            @PathVariable String homeUuid,
            @PathVariable String deviceUuid) {
        try {
            boolean success = homeService.unlinkDeviceFromHome(homeUuid, deviceUuid);
            return ResponseEntity.ok(success);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Internal API endpoints for microservice communication
    @GetMapping("/{homeUuid}/admin/{userUuid}")
    public ResponseEntity<Boolean> isUserAdminOfHome(
            @PathVariable String homeUuid,
            @PathVariable String userUuid) {
        boolean isAdmin = homeService.isUserAdminOfHome(userUuid, homeUuid);
        return ResponseEntity.ok(isAdmin);
    }

    @GetMapping("/{homeUuid}/guest/{userUuid}")
    public ResponseEntity<Boolean> isUserGuestOfHome(
            @PathVariable String homeUuid,
            @PathVariable String userUuid) {
        boolean isGuest = homeService.isUserGuestOfHome(userUuid, homeUuid);
        return ResponseEntity.ok(isGuest);
    }
} 