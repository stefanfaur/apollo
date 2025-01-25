package ro.faur.apollo.device.domain.dtos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.home.domain.Home;
import ro.faur.apollo.home.repository.HomeRepository;

@Component
public class DeviceDtoMapper {

    private final HomeRepository homeRepository;
    private final Logger logger = LoggerFactory.getLogger(DeviceDtoMapper.class);

    public DeviceDtoMapper(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    public DeviceDTO toDto(Device device) {
        return new DeviceDTO(
                device.getUuid(),
                device.getName(),
                device.getDeviceType(),
                device.getDescription(),
                device.getHardwareId(),
                device.getStatus(),
                device.getHome().getUuid()
        );
    }

    public Device toEntity(DeviceDTO deviceDTO) {
        return new Device(
                deviceDTO.getName(),
                deviceDTO.getDeviceType(),
                deviceDTO.getDescription(),
                deviceDTO.getHardwareId(),
                getHomeByUuid(deviceDTO.getHomeUuid()),
                deviceDTO.getStatus()
        );
    }

    private Home getHomeByUuid(String homeUuid) {
        Home home;
        try {
            home = homeRepository.findByUuid(homeUuid);
            if (home == null) {
                logger.warn("Home with UUID {} not found", homeUuid);
                throw new IllegalArgumentException("Home with UUID " + homeUuid + " not found");
            }
            return home;
        } catch (Exception e) {
            home = null;
            throw new IllegalArgumentException("Home with UUID " + homeUuid + " not found");
        }
    }
}
