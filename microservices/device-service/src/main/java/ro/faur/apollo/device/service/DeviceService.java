package ro.faur.apollo.device.service;

import org.springframework.stereotype.Service;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.shared.dto.DeviceStatus;
import ro.faur.apollo.device.dto.DeviceDTO;
import ro.faur.apollo.device.repository.DeviceRepository;
import ro.faur.apollo.shared.exception.DeviceException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<DeviceDTO> getAllDevices() {
        return deviceRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DeviceDTO getDevice(String deviceUuid) {
        Optional<Device> device = deviceRepository.findById(deviceUuid);
        return device.map(this::convertToDTO).orElse(null);
    }

    public DeviceDTO getDeviceByHardwareId(String hardwareId) {
        Device device = deviceRepository.findByHardwareId(hardwareId);
        return device != null ? convertToDTO(device) : null;
    }

    public List<DeviceDTO> getDevicesByHomeUuid(String homeUuid) {
        return deviceRepository.findByHomeUuid(homeUuid)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve devices for multiple homes in a single database query.
     */
    public List<DeviceDTO> getDevicesByHomeUuids(List<String> homeUuids) {
        if (homeUuids == null || homeUuids.isEmpty()) {
            return List.of();
        }
        return deviceRepository.findByHomeUuidIn(homeUuids)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DeviceDTO createDevice(String name, String deviceType, String description, String hardwareId, String homeUuid) {
        Device existingDevice = deviceRepository.findByHardwareId(hardwareId);
        if (existingDevice == null) {
            throw new DeviceException.DeviceNotRegisteredException("Device with hardware ID '" + hardwareId + "' is not registered. Please ensure the device is powered on and connected to the network.");
        }

        if (existingDevice.getHomeUuid() != null) {
            throw new DeviceException.DeviceAlreadyLinkedException("Device with hardware ID '" + hardwareId + "' is already linked to another home.");
        }

        // Update the existing device with home information
        existingDevice.setName(name);
        existingDevice.setDeviceType(deviceType);
        existingDevice.setDescription(description);
        existingDevice.setHomeUuid(homeUuid);
        existingDevice.setStatus(DeviceStatus.ONLINE); // Active since it's being linked

        Device savedDevice = deviceRepository.save(existingDevice);
        return convertToDTO(savedDevice);
    }

    public DeviceDTO registerDevice(String hardwareId, String deviceType) {
        Device existingDevice = deviceRepository.findByHardwareId(hardwareId);
        if (existingDevice != null) {
            return convertToDTO(existingDevice);
        }

        Device device = new Device();
        device.setHardwareId(hardwareId);
        device.setDeviceType(deviceType);
        device.setStatus(DeviceStatus.ONLINE);

        Device savedDevice = deviceRepository.save(device);
        return convertToDTO(savedDevice);
    }

    public boolean unlinkDeviceFromHome(String deviceUuid) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceUuid);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setHomeUuid(null);
            deviceRepository.save(device);
            return true;
        }
        return false;
    }

    public DeviceDTO updateDeviceStatus(String hardwareId, DeviceStatus status) {
        Device device = deviceRepository.findByHardwareId(hardwareId);
        if (device != null) {
            device.setStatus(status);
            Device savedDevice = deviceRepository.save(device);
            return convertToDTO(savedDevice);
        }
        return null;
    }

    private DeviceDTO convertToDTO(Device device) {
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
} 