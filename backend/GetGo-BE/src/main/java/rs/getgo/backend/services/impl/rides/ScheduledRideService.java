package rs.getgo.backend.services.impl.rides;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.RideCancellation;
import rs.getgo.backend.model.entities.WayPoint;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.RideCancellationRepository;
import rs.getgo.backend.services.DriverService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledRideService {

    private final ActiveRideRepository activeRideRepository;
    private final RideCancellationRepository rideCancellationRepository;
    private final DriverService driverService;
    private final WebSocketController webSocketController;

    // How many minutes before scheduled ride start should ride be activates
    private static final long ACTIVATION_MINUTES_BEFORE = 15L;

    // How many minutes after scheduled start time before ride is canceled (failed to assign driver multiple times)
    private static final long GRACE_PERIOD = 5;

    public ScheduledRideService(
            ActiveRideRepository activeRideRepository,
            RideCancellationRepository rideCancellationRepository,
            DriverService driverService,
            WebSocketController webSocketController
    ) {
        this.activeRideRepository = activeRideRepository;
        this.rideCancellationRepository = rideCancellationRepository;
        this.driverService = driverService;
        this.webSocketController = webSocketController;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processScheduledRides() {
        cancelOverdueScheduledRides();
        activateScheduledRides();
    }

    private void activateScheduledRides() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime activationThreshold = now.plusMinutes(ACTIVATION_MINUTES_BEFORE);

        List<ActiveRide> scheduledRides = activeRideRepository
                .findByStatusAndScheduledTimeLessThanEqual(
                        RideStatus.SCHEDULED,
                        activationThreshold
                );
        if (scheduledRides.isEmpty()) return;

        for (ActiveRide ride : scheduledRides) {
            try {
                activateScheduledRide(ride);
            } catch (Exception e) {
                System.err.println("Failed to activate scheduled ride " + ride.getId() + ": " + e.getMessage());
            }
        }
    }

    private void activateScheduledRide(ActiveRide ride) {
        Driver driver = driverService.findAvailableDriver(ride);
        // If no drivers were found, try in next scheduled run
        if (driver == null) return;

        ride.setDriver(driver);

        if (activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE)) {
            ride.setStatus(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE);
        } else {
            ride.setStatus(RideStatus.DRIVER_READY);
        }

        activeRideRepository.save(ride);

        // Notify driver if ready
        if (ride.getStatus() == RideStatus.DRIVER_READY) {
            GetDriverActiveRideDTO rideDTO = buildDriverActiveRideDTO(ride);
            webSocketController.notifyDriverRideAssigned(driver.getEmail(), rideDTO);
        }

        // Notify passenger
        webSocketController.notifyPassengerRideStatusUpdate(
                ride.getId(),
                ride.getStatus().toString(),
                ride.getStatus() == RideStatus.DRIVER_READY
                        ? "Driver assigned, your scheduled ride will start soon"
                        : "Driver assigned, waiting for driver to finish previous ride..."
        );
    }

    private GetDriverActiveRideDTO buildDriverActiveRideDTO(ActiveRide ride) {
        GetDriverActiveRideDTO dto = new GetDriverActiveRideDTO();
        dto.setRideId(ride.getId());
        dto.setStartingPoint(ride.getRoute().getStartingPoint());
        dto.setEndingPoint(ride.getRoute().getEndingPoint());
        dto.setEstimatedPrice(ride.getEstimatedPrice());
        dto.setEstimatedTimeMin(ride.getRoute().getEstTimeMin());
        dto.setPassengerName(ride.getPayingPassenger().getName() + " " + ride.getPayingPassenger().getSurname());
        dto.setPassengerCount(1 + (ride.getLinkedPassengers() != null ? ride.getLinkedPassengers().size() : 0));
        dto.setStatus(ride.getStatus().toString());
        dto.setScheduledTime(ride.getScheduledTime());

        dto.setLatitudes(ride.getRoute().getWaypoints().stream()
                .map(WayPoint::getLatitude)
                .toList());
        dto.setLongitudes(ride.getRoute().getWaypoints().stream()
                .map(WayPoint::getLongitude)
                .toList());
        dto.setAddresses(ride.getRoute().getWaypoints().stream()
                .map(WayPoint::getAddress)
                .toList());

        return dto;
    }

    private void cancelOverdueScheduledRides() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancellationThreshold = now.minusMinutes(GRACE_PERIOD);

        List<ActiveRide> overdueRides = activeRideRepository
                .findByStatusAndScheduledTimeLessThanEqual(
                        RideStatus.SCHEDULED,
                        cancellationThreshold
                );

        for (ActiveRide ride : overdueRides) {
            try {
                cancelOverdueRide(ride);
            } catch (Exception e) {
                System.err.println("Failed to cancel overdue ride " + ride.getId() + ": " + e.getMessage());
            }
        }
    }

    private void cancelOverdueRide(ActiveRide ride) {
        // Create and save ride cancellation
        RideCancellation cancellation = new RideCancellation();
        cancellation.setRideId(ride.getId());
        cancellation.setCancelerId(null);
        cancellation.setRole("SYSTEM");
        cancellation.setReason("Scheduled ride cancelled: No drivers available by scheduled time + grace period");
        cancellation.setCreatedAt(LocalDateTime.now());
        rideCancellationRepository.save(cancellation);

        // Delete active ride
        activeRideRepository.delete(ride);
    }
}