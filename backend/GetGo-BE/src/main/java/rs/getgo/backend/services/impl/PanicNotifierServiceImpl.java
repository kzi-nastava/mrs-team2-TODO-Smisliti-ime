package rs.getgo.backend.services.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.getgo.backend.dtos.panic.PanicAlertDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Panic;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.PanicRepository;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.services.PanicNotifierService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PanicNotifierServiceImpl implements PanicNotifierService {

    private final SimpMessagingTemplate messagingTemplate;
    private final PanicRepository panicRepository;
    private final ActiveRideRepository rideRepository;

    public PanicNotifierServiceImpl(SimpMessagingTemplate messagingTemplate,
                                    PanicRepository panicRepository,
                                    ActiveRideRepository rideRepository) {
        this.messagingTemplate = messagingTemplate;
        this.panicRepository = panicRepository;
        this.rideRepository = rideRepository;
    }

    @Override
    public void notifyAdmins(PanicAlertDTO dto) {
        // Push to admin WS topic
        messagingTemplate.convertAndSend("/socket-publisher/admin/panic", dto);
        // Additionally per-ride topic if admin subscribes per ride
        messagingTemplate.convertAndSend("/socket-publisher/admin/panic/" + dto.getRideId(), dto);
    }

    @Override
    public List<PanicAlertDTO> getUnreadPanics() {
        return panicRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getRead()))
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void markRead(Long panicId) {
        Panic p = panicRepository.findById(panicId)
                .orElseThrow(() -> new IllegalArgumentException("Panic not found"));
        p.setRead(true);
        panicRepository.save(p);
    }

    @Override
    public void markAllRead() {
        List<Panic> panics = panicRepository.findAll();
        for (Panic p : panics) {
            if (!Boolean.TRUE.equals(p.getRead())) {
                p.setRead(true);
            }
        }
        panicRepository.saveAll(panics);
    }

    private PanicAlertDTO mapToDTO(Panic p) {
        PanicAlertDTO dto = new PanicAlertDTO();
        dto.setPanicId(p.getId());
        dto.setRideId(p.getRideId());

        rideRepository.findById(p.getRideId())
                .ifPresentOrElse(
                        activeRide -> dto.setDriverId(activeRide.getDriver().getId()),
                        () -> dto.setDriverId(0L)
                );

        dto.setTriggeredByUserId(p.getTriggeredByUserId());
        dto.setTriggeredAt(p.getTriggeredAt());
        dto.setStatus(false);
        return dto;
    }
}
