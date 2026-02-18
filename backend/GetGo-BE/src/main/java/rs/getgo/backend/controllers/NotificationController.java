package rs.getgo.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.dtos.notification.NotificationDTO;
import rs.getgo.backend.repositories.NotificationRepository;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.services.NotificationService;
import rs.getgo.backend.utils.AuthUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository, NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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

    @PreAuthorize("hasRole('PASSENGER') or hasRole('DRIVER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<NotificationDTO> readNotification(@PathVariable Long id) {
        String email = AuthUtils.getCurrentUserEmail();
        Long userId = userRepository.findIdByEmail(email);

        NotificationDTO read = notificationService.readNotification(id, userId);
        if (read == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(read);
    }

    @PreAuthorize("hasRole('PASSENGER') or hasRole('DRIVER') or hasRole('ADMIN')")
    @PostMapping("/request-unread")
    public ResponseEntity<Void> requestUnread() {
        String email = AuthUtils.getCurrentUserEmail();
        if (email == null) {
            return ResponseEntity.badRequest().build();
        }
        Long userId = userRepository.findIdByEmail(email);
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }

        notificationService.getUserNotifications(userId);
        return ResponseEntity.ok().build();
    }

}
