package ro.faur.apollo.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.faur.apollo.home.domain.GuestDeviceRights;

import java.util.List;

@Repository
public interface GuestDeviceRightsRepository extends JpaRepository<GuestDeviceRights, String> {
    List<GuestDeviceRights> findByHomeGuestUuid(String homeGuestUuid);
    List<GuestDeviceRights> findByDeviceUuid(String deviceUuid);
    void deleteByHomeGuestUuidAndDeviceUuid(String homeGuestUuid, String deviceUuid);
} 