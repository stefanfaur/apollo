package ro.faur.apollo.device.service;

import org.springframework.stereotype.Service;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.device.repository.DeviceRepository;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
}
