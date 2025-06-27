package ro.faur.apollo.home.client;

import org.springframework.stereotype.Component;
import ro.faur.apollo.home.dto.DeviceDTO;

import java.util.Collections;
import java.util.List;

@Component
public class DeviceServiceClientFallback implements DeviceServiceClient {

    @Override
    public List<DeviceDTO> getAllDevices() {
        return Collections.emptyList();
    }

    @Override
    public DeviceDTO getDevice(String deviceUuid) {
        return null;
    }

    @Override
    public List<DeviceDTO> getDevicesByHome(String homeUuid) {
        return Collections.emptyList();
    }

    @Override
    public DeviceDTO createDevice(String name, String deviceType, String description, String hardwareId, String homeUuid) {
        return null;
    }

    @Override
    public Boolean unlinkDeviceFromHome(String deviceUuid) {
        return false;
    }

    @Override
    public List<DeviceDTO> getDevicesByHomeUuids(List<String> homeUuids) {
        return Collections.emptyList();
    }
} 