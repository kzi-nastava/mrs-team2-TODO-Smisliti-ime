package rs.getgo.backend.services.impl.rides;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.ride.GetActiveRideDTO;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.mappers.RideMapper;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.NotificationType;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.RideCancellationRepository;
import rs.getgo.backend.services.DriverMatchingService;
import rs.getgo.backend.services.NotificationService;
import rs.getgo.backend.services.RidePriceService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledRideService {

    private final ActiveRideRepository activeRideRepository;
    private final RideCancellationRepository rideCancellationRepository;
    private final DriverMatchingService driverMatchingService;
    private final RidePriceService ridePriceService;
    private final NotificationService notificationService;
    private final WebSocketController webSocketController;
    private final RideMapper rideMapper;

    // How many minutes before scheduled ride start should ride be activates
    private static final long ACTIVATION_MINUTES_BEFORE = 15L;

    // How many minutes after scheduled start time before ride is canceled (failed to assign driver multiple times)
    private static final long GRACE_PERIOD = 5;

    public ScheduledRideService(
            ActiveRideRepository activeRideRepository,
            RideCancellationRepository rideCancellationRepository,
            DriverMatchingService driverMatchingService,
            RidePriceService ridePriceService,
            NotificationService notificationService,
            WebSocketController webSocketController,
            RideMapper rideMapper
    ) {
        this.activeRideRepository = activeRideRepository;
        this.rideCancellationRepository = rideCancellationRepository;
        this.driverMatchingService = driverMatchingService;
        this.ridePriceService = ridePriceService;
        this.notificationService = notificationService;
        this.webSocketController = webSocketController;
        this.rideMapper = rideMapper;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendScheduledRideReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderWindow = now.plusMinutes(15);
        handleRideReminders(now, reminderWindow);
    }

    public void handleRideReminders(LocalDateTime now, LocalDateTime reminderWindow) {
        List<ActiveRide> upcomingRides = activeRideRepository
                .findByStatusAndScheduledTimeBetween(RideStatus.SCHEDULED, now, reminderWindow);

        for (ActiveRide ride : upcomingRides) {
            if (notificationService.wasRecentlySent(
                    ride.getPayingPassenger().getId(),
                    NotificationType.RIDE_STARTING_SOON,
                    4)) {
                continue;
            }

            notificationService.createAndNotify(
                    ride.getPayingPassenger().getId(),
                    NotificationType.RIDE_STARTING_SOON,
                    "Ride reminder",
                    "Your scheduled ride starts at " + ride.getScheduledTime().toLocalTime(),
                    LocalDateTime.now()
            );
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processScheduledRides() {
        cancelOverdueScheduledRides();
        activateScheduledRides();
    }

    public List<GetActiveRideDTO> getScheduledRides() {
        List<ActiveRide> rides = activeRideRepository.findByStatus(RideStatus.SCHEDULED);
        return rides.stream()
                .map(rideMapper::toGetActiveRideDTO)
                .toList();
    }

    public void activateScheduledRides() {
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
        Driver driver = driverMatchingService.findAvailableDriver(ride);
        if (driver == null) return;

        ride.setDriver(driver);
        determineVehicleType(ride, driver);
        determineRideStatus(ride, driver);
        activeRideRepository.save(ride);

        notifyDriverScheduledRideAssigned(ride);
        notifyPassengerScheduledDriverAssigned(ride);
    }

    private void determineVehicleType(ActiveRide ride, Driver driver) {
        if (ride.getVehicleType() != null) return;
        VehicleType vehicleType = driver.getVehicle().getType();
        ride.setVehicleType(vehicleType);
        ride.setEstimatedPrice(ridePriceService.calculateRidePrice(vehicleType, ride.getRoute().getEstDistanceKm()));
    }

    private void determineRideStatus(ActiveRide ride, Driver driver) {
        if (activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE)) {
            ride.setStatus(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE);
        } else {
            ride.setStatus(RideStatus.DRIVER_READY);
        }
    }

    private void notifyDriverScheduledRideAssigned(ActiveRide ride) {
        boolean ready = ride.getStatus() == RideStatus.DRIVER_READY;

        if (ready) {
            GetDriverActiveRideDTO rideDTO = rideMapper.toDriverActiveRideDTO(ride);
            webSocketController.notifyDriverRideAssigned(ride.getDriver().getEmail(), rideDTO);
        }

        notificationService.createAndNotify(
                ride.getDriver().getId(),
                NotificationType.DRIVER_ASSIGNED,
                ready ? "Scheduled ride assigned" : "Scheduled ride queued",
                ready ? "A scheduled ride has been assigned to you."
                        : "A scheduled ride has been assigned to you. It will start after your current ride.",
                LocalDateTime.now()
        );
    }

    private void notifyPassengerScheduledDriverAssigned(ActiveRide ride) {
        boolean ready = ride.getStatus() == RideStatus.DRIVER_READY;

        webSocketController.notifyPassengerRideStatusUpdate(
                ride.getId(),
                ride.getStatus().toString(),
                ready ? "Driver assigned, your scheduled ride will start soon"
                        : "Driver assigned, waiting for driver to finish previous ride..."
        );

        notificationService.createAndNotify(
                ride.getPayingPassenger().getId(),
                NotificationType.RIDE_ACCEPTED,
                "Driver assigned",
                ready ? "A driver has been assigned to your scheduled ride."
                        : "A driver has been assigned but is finishing a previous ride.",
                LocalDateTime.now()
        );
    }

    public void cancelOverdueScheduledRides() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancellationThreshold = now.minusMinutes(GRACE_PERIOD);

        List<ActiveRide> overdueRides = activeRideRepository
                .findByStatusAndScheduledTimeLessThanEqual(
                        RideStatus.SCHEDULED,
                        cancellationThreshold
                );

        for (ActiveRide ride : overdueRides) {
            try {
                cancelOverdueScheduledRide(ride);
            } catch (Exception e) {
                System.err.println("Failed to cancel overdue ride " + ride.getId() + ": " + e.getMessage());
            }
        }
    }

    private void cancelOverdueScheduledRide(ActiveRide ride) {
        RideCancellation cancellation = new RideCancellation();
        cancellation.setRideId(ride.getId());
        cancellation.setCancelerId(null);
        cancellation.setRole("SYSTEM");
        cancellation.setReason("Scheduled ride cancelled: No drivers available by scheduled time + grace period");
        cancellation.setCreatedAt(LocalDateTime.now());
        rideCancellationRepository.save(cancellation);

        notificationService.createAndNotify(
                ride.getPayingPassenger().getId(),
                NotificationType.RIDE_REJECTED,
                "Scheduled ride cancelled",
                "Your scheduled ride could not be fulfilled — no available drivers were found in time.",
                LocalDateTime.now()
        );

        // Delete active ride
        activeRideRepository.delete(ride);
    }
}