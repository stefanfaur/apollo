package ro.faur.apollo.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.faur.apollo.device.domain.Device;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    Device findByHardwareId(String hardwareId);
    List<Device> findByHomeUuid(String homeUuid);

    // Batch retrieval for multiple homes
    List<Device> findByHomeUuidIn(List<String> homeUuids);
} 