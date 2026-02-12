package rs.getgo.backend.services.impl.rides;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.ride.GetActiveRideDTO;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.mappers.RideMapper;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.RideCancellationRepository;
import rs.getgo.backend.services.DriverMatchingService;
import rs.getgo.backend.services.RidePriceService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledRideService {

    private final ActiveRideRepository activeRideRepository;
    private final RideCancellationRepository rideCancellationRepository;
    private final DriverMatchingService driverMatchingService;
    private final RidePriceService ridePriceService;
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
            WebSocketController webSocketController,
            RideMapper rideMapper
    ) {
        this.activeRideRepository = activeRideRepository;
        this.rideCancellationRepository = rideCancellationRepository;
        this.driverMatchingService = driverMatchingService;
        this.ridePriceService = ridePriceService;
        this.webSocketController = webSocketController;
        this.rideMapper = rideMapper;
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
        Driver driver = driverMatchingService.findAvailableDriver(ride);
        // If no drivers were found, try in next scheduled run
        if (driver == null) return;

        ride.setDriver(driver);

        // If vehicle type was 'ANY', set driver's vehicle type as ride vehicle type and calculate price
        if (ride.getVehicleType() == null) {
            VehicleType vehicleType = driver.getVehicle().getType();
            double routeEstDistanceKm = ride.getRoute().getEstDistanceKm();
            ride.setVehicleType(vehicleType);
            ride.setEstimatedPrice(ridePriceService.calculateRidePrice(vehicleType, routeEstDistanceKm));
        }

        if (activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE)) {
            ride.setStatus(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE);
        } else {
            ride.setStatus(RideStatus.DRIVER_READY);
        }

        activeRideRepository.save(ride);

        // Notify driver if ready
        if (ride.getStatus() == RideStatus.DRIVER_READY) {
            GetDriverActiveRideDTO rideDTO = rideMapper.toDriverActiveRideDTO(ride);
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
                cancelOverdueScheduledRide(ride);
            } catch (Exception e) {
                System.err.println("Failed to cancel overdue ride " + ride.getId() + ": " + e.getMessage());
            }
        }
    }

    private void cancelOverdueScheduledRide(ActiveRide ride) {
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