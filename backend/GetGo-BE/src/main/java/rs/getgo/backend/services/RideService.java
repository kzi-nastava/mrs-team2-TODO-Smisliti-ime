package rs.getgo.backend.services;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.ride.CancelRideDTO;
import rs.getgo.backend.dtos.rideStatus.CreatedRideStatusDTO;
import rs.getgo.backend.model.entities.RideCancellation;
import rs.getgo.backend.repositories.RideCancellationRepository;

import java.time.LocalDateTime;

@Service
public class RideService {

    private final RideCancellationRepository cancellationRepository;

    // passenger must cancel at least 10 minutes before scheduled start
    private static final long PASSENGER_CANCEL_MINUTES_BEFORE = 10L;

    public RideService(RideCancellationRepository cancellationRepository) {
        this.cancellationRepository = cancellationRepository;
    }

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

    public void estimateRide() {
        // TODO
    }

    public void stopRide() {
        // TODO
    }
}
