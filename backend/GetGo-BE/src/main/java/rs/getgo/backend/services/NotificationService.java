package rs.getgo.backend.services;

import rs.getgo.backend.model.entities.Notification;
import rs.getgo.backend.model.enums.NotificationType;

import java.time.LocalDateTime;

public interface NotificationService {
    Notification createAndNotify(Long userId, NotificationType type, String title, String message, LocalDateTime timestamp);
}
