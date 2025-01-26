package ro.faur.apollo.home.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.device.domain.dtos.CreateDeviceRequestDTO;
import ro.faur.apollo.device.domain.dtos.DeviceDTO;
import ro.faur.apollo.device.service.DeviceService;
import ro.faur.apollo.home.domain.dto.*;
import ro.faur.apollo.home.service.HomeAccessService;
import ro.faur.apollo.home.service.HomeService;

import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final HomeService homeService;
    private final HomeAccessService homeAccessService;
    private final DeviceService deviceService;

    public HomeController(HomeService homeService, HomeAccessService homeAccessService, DeviceService deviceService) {
        this.homeService = homeService;
        this.homeAccessService = homeAccessService;
        this.deviceService = deviceService;
    }

    @PostMapping()
    public ResponseEntity<HomeDTO> createHome(@RequestParam String name, @RequestParam String address) {
        return ResponseEntity.ok(homeService.createHome(name, address));
    }

    @GetMapping
    public ResponseEntity<List<HomeDTO>> getHomesForCurrentUser() {
        return ResponseEntity.ok(homeService.getHomesForCurrentUser());
    }

    @GetMapping("/{homeUuid}")
    public ResponseEntity<HomeDTO> getHome(@PathVariable String homeUuid) {
        HomeDTO home = homeService.getHome(homeUuid);
        return ResponseEntity.ok(home);
    }

    @DeleteMapping("/{homeUuid}")
    public ResponseEntity<?> deleteHome(@PathVariable String homeUuid) {
        try {
            homeService.deleteHome(homeUuid);
            return ResponseEntity.ok(true);
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
        return deviceService.createDeviceInHome(
                homeUuid,
                request.getName(),
                request.getDeviceType(),
                request.getDescription(),
                request.getHardwareId());
    }
}
