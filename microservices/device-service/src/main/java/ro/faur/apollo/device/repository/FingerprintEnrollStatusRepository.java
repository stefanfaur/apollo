package ro.faur.apollo.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.faur.apollo.device.domain.FingerprintEnrollStatus;

@Repository
public interface FingerprintEnrollStatusRepository extends JpaRepository<FingerprintEnrollStatus, String> {
    FingerprintEnrollStatus findByDeviceUuid(String deviceUuid);
} 