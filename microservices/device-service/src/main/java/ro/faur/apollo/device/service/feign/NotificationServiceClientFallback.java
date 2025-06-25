package ro.faur.apollo.device.service.feign;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationServiceClientFallback implements NotificationServiceClient {
    @Override
    public void startFingerprintEnroll(Map<String, Object> body) {
        // Fallback: do nothing
    }

    @Override
    public void sendUnlockCommand(Map<String, Object> body) {
        // Fallback: do nothing
    }
} 