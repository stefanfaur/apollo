package ro.faur.apollo.device.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.device.domain.dtos.DeviceDTO;
import ro.faur.apollo.device.service.DeviceService;

import java.util.List;

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
    public DeviceDTO getDevice(@PathVariable String deviceUuid) {
        return deviceService.getDevice(deviceUuid);
    }

    @PostMapping("/unlink/{deviceUuid}")
    public ResponseEntity<?> unlinkDeviceFromHome(@PathVariable String deviceUuid) {
        if (deviceService.unlinkDeviceFromHome(deviceUuid)) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.badRequest().body("Device not found for UUID: " + deviceUuid);
        }
    }

    @PostMapping("/{homeUuid}")
    public ResponseEntity<?> createDeviceInHome(@PathVariable String homeUuid, @RequestParam String name, @RequestParam String deviceType, @RequestParam String description, @RequestParam String hardwareId) {
        return deviceService.createDeviceInHome(homeUuid, name, deviceType, description, hardwareId);
    }
}
