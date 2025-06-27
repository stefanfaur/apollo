package ro.faur.apollo.notification.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DeviceServiceClientFallback implements FallbackFactory<DeviceServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceServiceClientFallback.class);

    @Override
    public DeviceServiceClient create(Throwable cause) {
        return new DeviceServiceClient() {
            @Override
            public void registerDevice(String hardwareId, String deviceType) {
                logger.error("DeviceServiceClient.registerDevice(hardwareId={}, deviceType={}) failed - falling back to no-op", 
                           hardwareId, deviceType, cause);
            }

            @Override
            public String getDeviceUuidByHardwareId(String hardwareId) {
                logger.error("DeviceServiceClient.getDeviceUuidByHardwareId({}) failed - falling back to null", hardwareId, cause);
                return null;
            }

            @Override
            public void updateEnrollStatus(String deviceUuid, Map<String, Object> body) {
                logger.error("DeviceServiceClient.updateEnrollStatus(deviceUuid={}, body={}) failed - falling back to no-op", 
                           deviceUuid, body, cause);
            }
        };
    }
} 