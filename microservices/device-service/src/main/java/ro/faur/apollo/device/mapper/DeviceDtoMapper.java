package ro.faur.apollo.device.mapper;

import org.springframework.stereotype.Component;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.device.dto.DeviceDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeviceDtoMapper {

    public DeviceDTO toDto(Device device) {
        if (device == null) {
            return null;
        }
        
        return new DeviceDTO(
                device.getUuid(),
                device.getName(),
                device.getDeviceType(),
                device.getDescription(),
                device.getHardwareId(),
                device.getStatus(),
                device.getHomeUuid()
        );
    }

    public List<DeviceDTO> toDto(List<Device> devices) {
        if (devices == null) {
            return null;
        }
        return devices.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Device toEntity(DeviceDTO deviceDTO) {
        if (deviceDTO == null) {
            return null;
        }
        
        Device device = new Device();
        device.setUuid(deviceDTO.getUuid());
        device.setName(deviceDTO.getName());
        device.setDeviceType(deviceDTO.getDeviceType());
        device.setDescription(deviceDTO.getDescription());
        device.setHardwareId(deviceDTO.getHardwareId());
        device.setStatus(deviceDTO.getStatus());
        device.setHomeUuid(deviceDTO.getHomeUuid());
        
        return device;
    }

    public List<Device> toEntity(List<DeviceDTO> deviceDTOs) {
        if (deviceDTOs == null) {
            return null;
        }
        return deviceDTOs.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
} 