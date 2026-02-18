package rs.getgo.backend.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.notification.NotificationDTO;
import rs.getgo.backend.model.entities.Notification;
import rs.getgo.backend.model.enums.NotificationType;
import rs.getgo.backend.repositories.NotificationRepository;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.services.NotificationService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WebSocketController webSocketController;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   WebSocketController webSocketController) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.webSocketController = webSocketController;
    }

    @Override
    public Notification createAndNotify(Long userId, NotificationType type, String title, String message, LocalDateTime timestamp) {
        Notification n = new Notification();
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        n.setTimestamp(timestamp != null ? timestamp : LocalDateTime.now());
        if (userId != null) {
            userRepository.findById(userId).ifPresent(n::setUser);
        }
        n.setRead(false);
        Notification saved = notificationRepository.save(n);

        // Build DTO and push over websocket to user (per-user topic by id)
        NotificationDTO dto = new NotificationDTO(saved.getId(), saved.getType(), saved.getTitle(), saved.getMessage(), saved.isRead(), saved.getTimestamp());
        try {
            webSocketController.notifyUserNotification(userId, dto);
        } catch (Exception ignored) {
            // best effort
        }

        return saved;
    }

    @Override
    public NotificationDTO readNotification(Long notificationId, Long userId) {
        if (notificationId == null) return null;
        Optional<Notification> opt = notificationRepository.findById(notificationId);
        if (!opt.isPresent()) return null;

        Notification n = opt.get();
        if (n.getUser() == null || !n.getUser().getId().equals(userId)) {
            // not owner => forbid/defer; return null for controller to handle as not found/forbidden
            return null;
        }

        n.setRead(true);

        notificationRepository.save(n);

        return new NotificationDTO(n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.isRead(), n.getTimestamp());
    }

    @Override
    public void getUserNotifications(Long userId) {
        if (userId == null) return;
        try {
            webSocketController.getUserNotifications(userId);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean wasRecentlySent(Long userId, NotificationType type, int minutesAgo) {
        return notificationRepository.existsByUserIdAndTypeAndTimestampAfter(
                userId, type, LocalDateTime.now().minusMinutes(minutesAgo)
        );
    }
}
