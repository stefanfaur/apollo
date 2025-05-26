package ro.faur.apollo.notification.client;

import org.springframework.stereotype.Component;

@Component
public class DeviceServiceClientFallback implements DeviceServiceClient {

    @Override
    public void registerDevice(String hardwareId, String deviceType) {
        // Fallback: do nothing when device service is unavailable
    }

    @Override
    public String getDeviceUuidByHardwareId(String hardwareId) {
        // Fallback: return null when device service is unavailable
        return null;
    }
} 