package ro.faur.apollo.device.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.device.dto.DeviceDTO;
import ro.faur.apollo.device.service.DeviceService;
import ro.faur.apollo.shared.exception.DeviceException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public List<DeviceDTO> getAllDevices() {
        return deviceService.getAllDevices();
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

    @PostMapping("/unlink/{deviceUuid}")
    public ResponseEntity<?> unlinkDeviceFromHome(@PathVariable String deviceUuid) {
        boolean success = deviceService.unlinkDeviceFromHome(deviceUuid);
        return success ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body("Device not found");
    }
}