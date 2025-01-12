package ro.faur.apollo.device.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.device.repository.DeviceRepository;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.repository.HomeRepository;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final HomeRepository homeRepository;

    public DeviceService(DeviceRepository deviceRepository, HomeRepository homeRepository) {
        this.deviceRepository = deviceRepository;
        this.homeRepository = homeRepository;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDevice(String deviceUuid) {
        return deviceRepository.findById(deviceUuid).orElse(null);
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
    public Device createDeviceInHome(String homeUuid, String name, String deviceType, String description, String hardwareId) {
        Device device = new Device(name, deviceType, description, hardwareId);
        Home home = homeRepository.findById(homeUuid).orElse(null);
        if (home == null) {
            throw new IllegalArgumentException("Home not found for UUID: " + homeUuid);
        }
        device.setHome(home);
        device = deviceRepository.save(device);
        home.getDevices().add(device);
        homeRepository.save(home);
        return device;
    }

}
