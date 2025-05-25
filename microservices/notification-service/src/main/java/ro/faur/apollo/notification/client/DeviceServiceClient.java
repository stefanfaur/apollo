package ro.faur.apollo.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "device-service", url = "${services.device.url:http://localhost:8082}")
public interface DeviceServiceClient {

    @PostMapping("/api/devices/register")
    void registerDevice(@RequestBody String hardwareId,
                        @RequestParam String deviceType);

    @GetMapping("/api/devices/hardware/{hardwareId}/uuid")
    String getDeviceUuidByHardwareId(@PathVariable String hardwareId);
} 