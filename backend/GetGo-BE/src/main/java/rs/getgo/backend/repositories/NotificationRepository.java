package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
