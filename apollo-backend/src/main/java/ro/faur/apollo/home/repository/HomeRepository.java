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
            "LEFT JOIN h.admins a " +
            "LEFT JOIN h.guests g " +
            "WHERE a.uuid = :uuid OR g.user.uuid = :uuid")
    List<Home> findByAdminOrGuest(@Param("uuid") String uuid);

    Home findByUuid(String uuid);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN TRUE ELSE FALSE END " +
            "FROM home h " +
            "JOIN h.admins a " +
            "WHERE a.uuid = :userUuid AND h.uuid = :homeUuid")
    boolean isUserAdminOfHome(String userUuid, String homeUuid);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN TRUE ELSE FALSE END " +
            "FROM home h " +
            "JOIN h.guests g " +
            "WHERE g.user.uuid = :userUuid AND h.uuid = :homeUuid")
    boolean isUserGuestOfHome(String userUuid, String homeUuid);
}
