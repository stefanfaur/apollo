package ro.faur.apollo.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.faur.apollo.device.domain.FingerprintEnrollStatus;

@Repository
public interface FingerprintEnrollStatusRepository extends JpaRepository<FingerprintEnrollStatus, String> {
    FingerprintEnrollStatus findByDeviceUuid(String deviceUuid);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO fingerprint_enroll_status (uuid, device_uuid, template_id, status, error_code, created_at, updated_at, deleted)
        VALUES (gen_random_uuid(), :deviceUuid, :templateId, :status, :errorCode, NOW(), NOW(), false)
        ON CONFLICT (device_uuid) DO UPDATE SET
            template_id = EXCLUDED.template_id,
            status = EXCLUDED.status,
            error_code = EXCLUDED.error_code,
            updated_at = NOW()
        """, nativeQuery = true)
    void upsertStatus(@Param("deviceUuid") String deviceUuid, 
                     @Param("templateId") Integer templateId,
                     @Param("status") String status,
                     @Param("errorCode") String errorCode);
} 