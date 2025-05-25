package ro.faur.apollo.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.faur.apollo.home.domain.HomeGuest;

import java.util.List;

@Repository
public interface HomeGuestRepository extends JpaRepository<HomeGuest, String> {
    List<HomeGuest> findByHomeUuid(String homeUuid);
    List<HomeGuest> findByUserUuid(String userUuid);
    void deleteByHomeUuidAndUserUuid(String homeUuid, String userUuid);
} 