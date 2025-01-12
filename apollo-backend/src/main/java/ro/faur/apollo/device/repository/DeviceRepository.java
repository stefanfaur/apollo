package ro.faur.apollo.device.repository;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.faur.apollo.device.domain.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {

    @Cacheable(value = "deviceCache", key = "#hardwareId", unless = "#result == null")
    Device findByHardwareId(String hardwareId);

    @Override
    @CachePut(value = "deviceCache", key = "#result.hardwareId")
    Device save(Device device);
}
