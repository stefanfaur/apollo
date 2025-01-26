package ro.faur.apollo.device.service;

import jakarta.transaction.Transactional;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.device.domain.dtos.DeviceDTO;
import ro.faur.apollo.device.domain.dtos.DeviceDtoMapper;
import ro.faur.apollo.device.repository.DeviceRepository;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.repository.HomeRepository;

import java.util.List;
import java.util.Optional;

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
     * <p>Unlink all devices that belonged to a home.</p>
     * <p>Devices are not deleted, only their home reference is removed.</p>
     * <p>Usually done when deleting a home.</p>
     * @param home
     * @return
     */
    @Transactional
    public boolean unlinkDevicesFromHome(Home home) {

        List<Device> devices = home.getDevices();
        for (Device device : devices) {
            device.setHome(null);
            deviceRepository.save(device);
        }

        home.getDevices().clear();
        homeRepository.save(home);

        return true;
    }

    /**
     * <p>Unlink a device from a home.</p>
     * <p>Device is not deleted, only its home reference is removed.</p>
     * @param deviceUuid
     * @return
     */
    public boolean unlinkDeviceFromHome(String deviceUuid) {
        Device device = deviceRepository.findById(deviceUuid).orElse(null);
        if (device == null) {
            throw new IllegalArgumentException("Device not found for UUID: " + deviceUuid);
        }
        device.setHome(null);
        device.getHome().getDevices().remove(device);
        deviceRepository.save(device);
        homeRepository.save(device.getHome());
        return true;
    }

    /**
     * <p>Link a device to a home.</p>
     * <p>The device itself already exists in the database once it is turned on and connected to the internet.</p>
     * @param homeUuid
     * @param name
     * @param hardwareId
     * @return DeviceDTO if successful, else a bad request response.
     */
    @Transactional
    public ResponseEntity createDeviceInHome(String homeUuid, String name, String deviceType, String description, String hardwareId) {
        Home home = homeRepository.findById(homeUuid).orElse(null);
        if (home == null) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }

        Device device = deviceRepository.findByHardwareId(hardwareId);
        if (device == null) {
            return ResponseEntity.badRequest().body("Device not found for hardwareId '" + hardwareId + "' , please turn on the device and connect it to the internet.");
        }

        device.setHome(home);
        device = deviceRepository.save(device);
        home.getDevices().add(device);
        homeRepository.save(home);

        return ResponseEntity.ok(deviceDtoMapper.toDto(device));
    }

}
