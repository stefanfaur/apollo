package ro.faur.apollo.device.service.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationServiceClientFallback implements FallbackFactory<NotificationServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceClientFallback.class);

    @Override
    public NotificationServiceClient create(Throwable cause) {
        return new NotificationServiceClient() {
            @Override
            public void startFingerprintEnroll(Map<String, Object> body) {
                logger.error("NotificationServiceClient.startFingerprintEnroll(body={}) failed - falling back to no-op", body, cause);
            }

            @Override
            public void sendUnlockCommand(Map<String, Object> body) {
                logger.error("NotificationServiceClient.sendUnlockCommand(body={}) failed - falling back to no-op", body, cause);
            }
        };
    }
} 