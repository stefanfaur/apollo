package ro.faur.apollo.device.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.device.domain.dtos.DeviceDTO;
import ro.faur.apollo.device.domain.dtos.DeviceDtoMapper;
import ro.faur.apollo.device.repository.DeviceRepository;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.repository.HomeRepository;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final HomeRepository homeRepository;
    private final DeviceDtoMapper deviceDtoMapper;

    public DeviceService(DeviceRepository deviceRepository, HomeRepository homeRepository, DeviceDtoMapper deviceDtoMapper) {
        this.deviceRepository = deviceRepository;
        this.homeRepository = homeRepository;
        this.deviceDtoMapper = deviceDtoMapper;
    }

    public List<DeviceDTO> getAllDevices() {
        return deviceRepository.findAll()
                .stream()
                .map(deviceDtoMapper::toDto)
                .toList();
    }

    public DeviceDTO getDevice(String deviceUuid) {
        Device device = deviceRepository.findById(deviceUuid).orElse(null);
        if (device == null) {
            throw new IllegalArgumentException("Device not found for UUID: " + deviceUuid);
        }
        return deviceDtoMapper.toDto(device);
    }

    /**
     * Create a device in a home.
     * This method will be removed in the future, as devices will be created by the device itself
     * via initial mqtt message, and only will be linked to a home later.
     * @param homeUuid
     * @param name
     * @param hardwareId
     * @return
     */
    @Transactional
    public DeviceDTO createDeviceInHome(String homeUuid, String name, String deviceType, String description, String hardwareId) {
        Device device = new Device(name, deviceType, description, hardwareId);
        Home home = homeRepository.findById(homeUuid).orElse(null);
        if (home == null) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        device.setHome(home);
        device = deviceRepository.save(device);
        home.getDevices().add(device);
        homeRepository.save(home);

        return deviceDtoMapper.toDto(device);
    }

}
