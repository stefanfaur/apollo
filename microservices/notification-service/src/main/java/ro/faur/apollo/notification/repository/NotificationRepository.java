package ro.faur.apollo.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.faur.apollo.notification.domain.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByDeviceUuid(String deviceUuid);
    
    List<Notification> findAllByOrderByCreatedAtDesc();
} 