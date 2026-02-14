package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Notification;
import rs.getgo.backend.model.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByTimestampDesc(Long userId);
    boolean existsByUserIdAndTypeAndTimestampAfter(Long userId, NotificationType type, LocalDateTime after);
}
