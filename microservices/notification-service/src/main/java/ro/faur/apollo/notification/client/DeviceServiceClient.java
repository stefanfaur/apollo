package ro.faur.apollo.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "device-service",
    url = "${services.device.url:}",
    fallback = DeviceServiceClientFallback.class
)
@Primary
public interface DeviceServiceClient {

    @PostMapping("/api/devices/register")
    void registerDevice(@RequestBody String hardwareId,
                        @RequestParam String deviceType);

    @GetMapping("/api/devices/hardware/{hardwareId}/uuid")
    String getDeviceUuidByHardwareId(@PathVariable String hardwareId);
} 