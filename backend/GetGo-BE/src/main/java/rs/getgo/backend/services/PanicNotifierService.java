package rs.getgo.backend.services;

import rs.getgo.backend.dtos.panic.PanicAlertDTO;

import java.util.List;

public interface PanicNotifierService {
    // Publish a new panic event to admin topic(s)
    void notifyAdmins(PanicAlertDTO dto);

    // Return unread panic alerts (basic fields mapped from Panic entity)
    List<PanicAlertDTO> getUnreadPanics();

    // Mark single panic as read
    void markRead(Long panicId);

    // Mark all panics as read
    void markAllRead();
}
