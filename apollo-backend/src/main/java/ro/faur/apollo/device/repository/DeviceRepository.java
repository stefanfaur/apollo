package ro.faur.apollo.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.faur.apollo.device.domain.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
}
