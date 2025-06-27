package ro.faur.apollo.device.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.device.dto.DeviceDTO;
import ro.faur.apollo.device.service.DeviceService;
import ro.faur.apollo.device.service.FingerprintEnrollService;
import ro.faur.apollo.device.service.feign.NotificationServiceClient;
import ro.faur.apollo.shared.exception.DeviceException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final FingerprintEnrollService fingerprintEnrollService;
    private final NotificationServiceClient notificationServiceClient;

    public DeviceController(DeviceService deviceService,
                            FingerprintEnrollService fingerprintEnrollService,
                            NotificationServiceClient notificationServiceClient) {
        this.deviceService = deviceService;
        this.fingerprintEnrollService = fingerprintEnrollService;
        this.notificationServiceClient = notificationServiceClient;
    }

    @GetMapping("/{deviceUuid}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable String deviceUuid) {
        DeviceDTO device = deviceService.getDevice(deviceUuid);
        return device != null ? ResponseEntity.ok(device) : ResponseEntity.notFound().build();
    }

    @GetMapping("/hardware/{hardwareId}")
    public ResponseEntity<DeviceDTO> getDeviceByHardwareId(@PathVariable String hardwareId) {
        DeviceDTO device = deviceService.getDeviceByHardwareId(hardwareId);
        return device != null ? ResponseEntity.ok(device) : ResponseEntity.notFound().build();
    }

    @GetMapping("/hardware/{hardwareId}/uuid")
    public ResponseEntity<String> getDeviceUuidByHardwareId(@PathVariable String hardwareId) {
        DeviceDTO device = deviceService.getDeviceByHardwareId(hardwareId);
        return device != null ? ResponseEntity.ok(device.getUuid()) : ResponseEntity.notFound().build();
    }

    @GetMapping("/home/{homeUuid}")
    public List<DeviceDTO> getDevicesByHome(@PathVariable String homeUuid) {
        return deviceService.getDevicesByHomeUuid(homeUuid);
    }

    /**
     * Batch endpoint to retrieve devices for multiple homes.
     */
    @GetMapping("/by-homes")
    public List<DeviceDTO> getDevicesByHomeUuids(@RequestParam List<String> homeUuids) {
        return deviceService.getDevicesByHomeUuids(homeUuids);
    }

    @PostMapping
    public ResponseEntity<?> createDevice(
            @RequestParam String name,
            @RequestParam String deviceType,
            @RequestParam String description,
            @RequestParam String hardwareId,
            @RequestParam String homeUuid) {
        try {
            DeviceDTO device = deviceService.createDevice(name, deviceType, description, hardwareId, homeUuid);
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

    @PostMapping("/register")
    public ResponseEntity<DeviceDTO> registerDevice(
            @RequestBody String hardwareId,
            @RequestParam String deviceType) {
        DeviceDTO device = deviceService.registerDevice(hardwareId, deviceType);
        return ResponseEntity.ok(device);
    }

    @PostMapping("/{deviceUuid}/fingerprint/enroll")
    public ResponseEntity<?> startFingerprintEnroll(@PathVariable String deviceUuid,
                                                    @RequestBody Map<String, Integer> body) {
        Integer userFpId = body.get("userFpId");
        if (userFpId == null || userFpId < 1 || userFpId > 127) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "INVALID_TEMPLATE_ID",
                    "message", "userFpId must be between 1 and 127"
            ));
        }

        var device = deviceService.getDevice(deviceUuid);
        if (device == null) {
            return ResponseEntity.notFound().build();
        }

        // Offload the potentially slow operation to a background thread
        java.util.concurrent.CompletableFuture.runAsync(() ->
                fingerprintEnrollService.startEnrollment(deviceUuid, userFpId, device.getHardwareId())
        );

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{deviceUuid}/fingerprint/enroll/status")
    public ResponseEntity<Map<String, String>> getEnrollStatus(@PathVariable String deviceUuid) {
        var status = fingerprintEnrollService.getStatus(deviceUuid);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("status", status.name().toLowerCase()));
    }

    @PostMapping("/{deviceUuid}/fingerprint/enroll/status")
    public ResponseEntity<?> updateEnrollStatus(@PathVariable String deviceUuid,
                                                @RequestBody Map<String, Object> body) {
        String statusStr = (String) body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().body("status required");
        }
        var statusEnum = switch (statusStr.toLowerCase()) {
            case "success" -> ro.faur.apollo.device.domain.EnrollStatus.SUCCESS;
            case "failure" -> ro.faur.apollo.device.domain.EnrollStatus.FAILURE;
            default -> null;
        };
        if (statusEnum == null) {
            return ResponseEntity.badRequest().body("Invalid status value");
        }

        fingerprintEnrollService.updateStatus(deviceUuid, statusEnum, (String) body.get("errorCode"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unlink/{deviceUuid}")
    public ResponseEntity<?> unlinkDeviceFromHome(@PathVariable String deviceUuid) {
        boolean success = deviceService.unlinkDeviceFromHome(deviceUuid);
        return success ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body("Device not found");
    }

    @PostMapping("/{deviceUuid}/unlock")
    public ResponseEntity<?> remoteUnlock(@PathVariable String deviceUuid) {
        var device = deviceService.getDevice(deviceUuid);
        if (device == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> body = Map.of("hardwareId", device.getHardwareId());

        // Fire-and-forget: perform the Feign call asynchronously so the client doesn't wait
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                notificationServiceClient.sendUnlockCommand(body);
            } catch (Exception e) {
                // Just log the error; we don't want to fail the user's request
                System.err.println("Failed to send unlock command for device " + deviceUuid + ": " + e.getMessage());
            }
        });

        return ResponseEntity.accepted().build();
    }
}