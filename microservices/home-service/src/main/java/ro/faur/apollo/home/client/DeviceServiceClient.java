package ro.faur.apollo.home.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.home.dto.DeviceDTO;

import java.util.List;

@FeignClient(
    name = "device-service",
    url = "${services.device.url:}",
    fallback = DeviceServiceClientFallback.class
)
public interface DeviceServiceClient {

    @GetMapping("/api/devices")
    List<DeviceDTO> getAllDevices();

    @GetMapping("/api/devices/{deviceUuid}")
    DeviceDTO getDevice(@PathVariable String deviceUuid);

    @GetMapping("/api/devices/home/{homeUuid}")
    List<DeviceDTO> getDevicesByHome(@PathVariable String homeUuid);

    @PostMapping("/api/devices")
    DeviceDTO createDevice(@RequestParam String name,
                          @RequestParam String deviceType,
                          @RequestParam String description,
                          @RequestParam String hardwareId,
                          @RequestParam String homeUuid);

    @PostMapping("/api/devices/unlink/{deviceUuid}")
    Boolean unlinkDeviceFromHome(@PathVariable String deviceUuid);

    @GetMapping("/api/devices/by-homes")
    List<DeviceDTO> getDevicesByHomeUuids(@RequestParam("homeUuids") List<String> homeUuids);
} 