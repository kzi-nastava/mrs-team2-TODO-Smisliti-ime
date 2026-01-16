package rs.getgo.backend.services.Impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.ride.CancelRideDTO;
import rs.getgo.backend.dtos.rideStatus.CreatedRideStatusDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Panic;
import rs.getgo.backend.model.entities.RideCancellation;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.PanicRepository;
import rs.getgo.backend.repositories.RideCancellationRepository;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.services.RideService;

import java.time.LocalDateTime;

@Service
public class RideServiceImpl implements RideService {

    private final RideCancellationRepository cancellationRepository;
    private final PanicRepository panicRepository;
    private final ActiveRideRepository activeRideRepository;
    private final UserRepository userRepository;

    // passenger must cancel at least 10 minutes before scheduled start
    private static final long PASSENGER_CANCEL_MINUTES_BEFORE = 10L;

    public RideServiceImpl(RideCancellationRepository cancellationRepository, PanicRepository panicRepository, ActiveRideRepository activeRideRepository, UserRepository userRepository) {
        this.cancellationRepository = cancellationRepository;
        this.panicRepository = panicRepository;
        this.activeRideRepository = activeRideRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CreatedRideStatusDTO cancelRide(Long rideId, CancelRideDTO req) {
        String role = req.getRole() != null ? req.getRole().toUpperCase() : "PASSENGER";

        if ("DRIVER".equals(role)) {
            // driver can cancel only before passengers enter the vehicle
            if (Boolean.TRUE.equals(req.getPassengersEntered())) {
                throw new IllegalStateException("Driver cannot cancel after passengers entered");
            }
        } else {
            // passenger cancellation rule: scheduled start must exist and cancellation >= 10 minutes before
            if (req.getScheduledStartTime() == null) {
                throw new IllegalStateException("Passenger cancellation requires scheduled start time");
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime cutoff = req.getScheduledStartTime().minusMinutes(PASSENGER_CANCEL_MINUTES_BEFORE);
            if (!now.isBefore(cutoff)) {
                throw new IllegalStateException("Too late to cancel (must cancel at least 10 minutes before start)");
            }
        }

        // persist cancellation
        RideCancellation rc = new RideCancellation();
        rc.setRideId(rideId);
        rc.setCancelerId(req.getCancelerId());
        rc.setRole(role);
        rc.setReason(req.getReason());
        rc.setCreatedAt(LocalDateTime.now());
        cancellationRepository.save(rc);

        return new CreatedRideStatusDTO(rideId, "CANCELED");
    }

    @Override
    public void stopRide() {
        // TODO
    }

    @Override
    public void triggerPanic(Long rideId, String email) {

        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride not found"));

        Panic panic = new Panic();
        panic.setRide(ride);
        panic.setTriggeredByUserId(userRepository.findIdByEmail(email));
        panic.setTriggeredAt(LocalDateTime.now());

        panicRepository.save(panic);

        // TODO: notificationService.notifyAdminsAboutPanic(panic);
    }
}
