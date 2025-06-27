package ro.faur.apollo.device.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "notification-service",
        url = "${services.notification.url:}",
        fallbackFactory = NotificationServiceClientFallback.class
)
@Primary
public interface NotificationServiceClient {

    @PostMapping("/internal/mqtt/fingerprint/enroll/start")
    void startFingerprintEnroll(@RequestBody Map<String, Object> body);

    @PostMapping("/internal/mqtt/unlock")
    void sendUnlockCommand(@RequestBody Map<String, Object> body);
} 