package rs.getgo.backend.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.model.entities.Notification;
import rs.getgo.backend.model.enums.NotificationType;
import rs.getgo.backend.repositories.NotificationRepository;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.services.NotificationService;

import java.time.LocalDateTime;

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
        Notification saved = notificationRepository.save(n);

        // Push over websocket to user (per-user topic by id)
        try {
            webSocketController.notifyUserNotification(userId, saved);
        } catch (Exception ignored) {
            // best effort
        }

        return saved;
    }
}
