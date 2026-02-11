package rs.getgo.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.getgo.backend.dtos.notification.NotificationDTO;
import rs.getgo.backend.repositories.NotificationRepository;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.utils.AuthUtils;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('PASSENGER') or hasRole('DRIVER') or hasRole('ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications() {
        String email = AuthUtils.getCurrentUserEmail();
        Long userId = userRepository.findIdByEmail(email);
        List<NotificationDTO> list = notificationRepository.findByUserIdOrderByTimestampDesc(userId).stream().map(n -> new NotificationDTO(
                n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.isRead(), n.getTimestamp()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}

