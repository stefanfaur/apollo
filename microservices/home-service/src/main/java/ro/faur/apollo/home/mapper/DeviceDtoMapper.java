package ro.faur.apollo.home.mapper;

import org.springframework.stereotype.Component;
import ro.faur.apollo.home.dto.DeviceDTO;
import ro.faur.apollo.shared.dto.DeviceStatus;

@Component
public class DeviceDtoMapper {

    public DeviceDTO createDeviceDTO(String uuid, String name, String deviceType,
                                   String description, String hardwareId, 
                                   DeviceStatus status, String homeUuid) {
        return new DeviceDTO(uuid, name, deviceType, description, hardwareId, status, homeUuid);
    }

    public DeviceDTO createBasicDeviceDTO(String uuid, String name, String deviceType, String homeUuid) {
        return new DeviceDTO(uuid, name, deviceType, null, null, DeviceStatus.UNKNOWN, homeUuid);
    }

    public DeviceDTO updateStatus(DeviceDTO deviceDTO, DeviceStatus newStatus) {
        if (deviceDTO == null) {
            return null;
        }
        deviceDTO.setStatus(newStatus);
        return deviceDTO;
    }
} 