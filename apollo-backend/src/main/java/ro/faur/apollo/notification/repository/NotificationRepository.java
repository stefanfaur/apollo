package ro.faur.apollo.notification.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.faur.apollo.notification.domain.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT n FROM Notification n WHERE n.emitter.hardwareId = :hardwareId")
    List<Notification> findByDeviceHardwareId(@Param("hardwareId") String hardwareId);

    @Query("SELECT n FROM Notification n WHERE n.emitter.uuid = :deviceUuid")
    List<Notification> findByDeviceUuid(@Param("deviceUuid") String deviceUuid);


    @Query("""
        SELECT n FROM Notification n 
        JOIN n.emitter d 
        JOIN d.home h 
        LEFT JOIN h.admins a 
        LEFT JOIN h.guests g 
        WHERE a.uuid = :userUuid OR g.user.uuid = :userUuid
    """)
    List<Notification> findByUserUuid(@Param("userUuid") String userUuid, Sort sort);

}
