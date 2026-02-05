package rs.getgo.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.dtos.panic.PanicAlertDTO;
import rs.getgo.backend.services.PanicNotifierService;

import java.util.List;

@RestController
@RequestMapping("/api/panic")
@CrossOrigin(origins = "http://localhost:4200")
public class PanicNotifierController {

    private final PanicNotifierService panicNotifierService;

    public PanicNotifierController(PanicNotifierService panicNotifierService) {
        this.panicNotifierService = panicNotifierService;
    }

    // Admin pulls unread panic alerts (in case WS missed)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/unread")
    public List<PanicAlertDTO> getUnread() {
        return panicNotifierService.getUnreadPanics();
    }

    // Admin marks a single panic as read (removes "unsafe" badge in UI)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/read/{panicId}")
    public ResponseEntity<Void> markRead(@PathVariable Long panicId) {
        panicNotifierService.markRead(panicId);
        return ResponseEntity.ok().build();
    }

    // Admin marks all panic messages as read
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/read-all")
    public ResponseEntity<Void> markAllRead() {
        panicNotifierService.markAllRead();
        return ResponseEntity.ok().build();
    }
}
