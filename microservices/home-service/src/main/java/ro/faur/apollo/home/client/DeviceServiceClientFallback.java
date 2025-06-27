package ro.faur.apollo.home.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ro.faur.apollo.home.dto.DeviceDTO;

import java.util.Collections;
import java.util.List;

@Component
public class DeviceServiceClientFallback implements FallbackFactory<DeviceServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceServiceClientFallback.class);

    @Override
    public DeviceServiceClient create(Throwable cause) {
        return new DeviceServiceClient() {
            @Override
            public List<DeviceDTO> getAllDevices() {
                logger.error("DeviceServiceClient.getAllDevices() failed - falling back to empty list", cause);
                return Collections.emptyList();
            }

            @Override
            public DeviceDTO getDevice(String deviceUuid) {
                logger.error("DeviceServiceClient.getDevice({}) failed - falling back to null", deviceUuid, cause);
                return null;
            }

            @Override
            public List<DeviceDTO> getDevicesByHome(String homeUuid) {
                logger.error("DeviceServiceClient.getDevicesByHome({}) failed - falling back to empty list", homeUuid, cause);
                return Collections.emptyList();
            }

            @Override
            public DeviceDTO createDevice(String name, String deviceType, String description, String hardwareId, String homeUuid) {
                logger.error("DeviceServiceClient.createDevice(name={}, type={}, hardwareId={}, homeUuid={}) failed - falling back to null", 
                           name, deviceType, hardwareId, homeUuid, cause);
                return null;
            }

            @Override
            public Boolean unlinkDeviceFromHome(String deviceUuid) {
                logger.error("DeviceServiceClient.unlinkDeviceFromHome({}) failed - falling back to false", deviceUuid, cause);
                return false;
            }

            @Override
            public List<DeviceDTO> getDevicesByHomeUuids(List<String> homeUuids) {
                logger.error("DeviceServiceClient.getDevicesByHomeUuids({}) failed - falling back to empty list", homeUuids, cause);
                return Collections.emptyList();
            }
        };
    }
} 