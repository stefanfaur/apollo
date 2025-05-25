package ro.faur.apollo.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.faur.apollo.home.domain.Home;

import java.util.List;

@Repository
public interface HomeRepository extends JpaRepository<Home, String> {

    @Query("SELECT h FROM home h " +
           "WHERE :userUuid MEMBER OF h.adminUuids " +
           "OR EXISTS (SELECT g FROM HomeGuest g WHERE g.homeUuid = h.uuid AND g.userUuid = :userUuid)")
    List<Home> findByAdminOrGuest(@Param("userUuid") String userUuid);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN TRUE ELSE FALSE END " +
           "FROM home h " +
           "WHERE h.uuid = :homeUuid AND :userUuid MEMBER OF h.adminUuids")
    boolean isUserAdminOfHome(@Param("userUuid") String userUuid, @Param("homeUuid") String homeUuid);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN TRUE ELSE FALSE END " +
           "FROM HomeGuest g " +
           "WHERE g.userUuid = :userUuid AND g.homeUuid = :homeUuid")
    boolean isUserGuestOfHome(@Param("userUuid") String userUuid, @Param("homeUuid") String homeUuid);
} 