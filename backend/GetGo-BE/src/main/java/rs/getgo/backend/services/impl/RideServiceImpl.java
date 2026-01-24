package rs.getgo.backend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.ride.*;
import rs.getgo.backend.dtos.rideStatus.CreatedRideStatusDTO;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RideOrderStatus;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.*;
import rs.getgo.backend.services.DriverService;
import rs.getgo.backend.services.EmailService;
import rs.getgo.backend.services.RideService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RideServiceImpl implements RideService {

    private final RideCancellationRepository cancellationRepository;
    private final PanicRepository panicRepository;
    private final ActiveRideRepository activeRideRepository;
    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final RouteRepository routeRepository;
    private final DriverRepository driverRepository;
    private final DriverService driverService;
    private final CompletedRideRepository completedRideRepository;
    private final EmailService emailService;


    // passenger must cancel at least 10 minutes before scheduled start
    private static final long PASSENGER_CANCEL_MINUTES_BEFORE = 10L;

    public RideServiceImpl(RideCancellationRepository cancellationRepository,
                           PanicRepository panicRepository,
                           ActiveRideRepository activeRideRepository,
                           UserRepository userRepository,
                           PassengerRepository passengerRepository,
                           RouteRepository routeRepository,
                           DriverRepository driverRepository,
                           DriverService driverService,
                           CompletedRideRepository completedRideRepository,
                           EmailService emailService) {
        this.cancellationRepository = cancellationRepository;
        this.panicRepository = panicRepository;
        this.activeRideRepository = activeRideRepository;
        this.userRepository = userRepository;
        this.passengerRepository = passengerRepository;
        this.routeRepository = routeRepository;
        this.driverRepository = driverRepository;
        this.driverService = driverService;
        this.completedRideRepository = completedRideRepository;
        this.emailService = emailService;
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
    public CreatedRideResponseDTO orderRide(CreateRideRequestDTO createRideRequestDTO, String userEmail) {
        // TODO: DO WITH VALIDATORS
        // 1. Validate request
        if (createRideRequestDTO.getLatitudes().size() < 2 ||
                createRideRequestDTO.getLatitudes().size() != createRideRequestDTO.getLongitudes().size() ||
                createRideRequestDTO.getLatitudes().size() != createRideRequestDTO.getAddresses().size()) {
            return new CreatedRideResponseDTO(
                    "INVALID_REQUEST",
                    "Invalid coordinates or addresses",
                    null
            );
        }

        // 2. Find paying passenger
        Passenger payingPassenger = passengerRepository.findByEmail(userEmail)
                .orElse(null);
        if (payingPassenger == null) {
            return new CreatedRideResponseDTO(
                    RideOrderStatus.PASSENGER_NOT_FOUND.toString(),
                    "Passenger account not found",
                    null
            );
        }

        // 3. Parse scheduled time
        LocalDateTime scheduledTime = null;
        if (createRideRequestDTO.getScheduledTime() != null && !createRideRequestDTO.getScheduledTime().isEmpty()) {
            scheduledTime = parseScheduledTime(createRideRequestDTO.getScheduledTime());

            if (scheduledTime == null ||
                    scheduledTime.isBefore(LocalDateTime.now()) ||
                    scheduledTime.isAfter(LocalDateTime.now().plusHours(5))) {
                return new CreatedRideResponseDTO(
                        RideOrderStatus.INVALID_SCHEDULED_TIME.toString(),
                        "Scheduled time must be within the next 5 hours",
                        null
                );
            }
        }

        // 4. Find linked passengers
        List<Passenger> linkedPassengers = new ArrayList<>();
        if (createRideRequestDTO.getFriendEmails() != null) {
            for (String email : createRideRequestDTO.getFriendEmails()) {
                Passenger passenger = passengerRepository.findByEmail(email).orElse(null);
                if (passenger == null) {
                    return new CreatedRideResponseDTO(
                            "LINKED_PASSENGER_NOT_FOUND",
                            "Passenger with email " + email + " not found",
                            null
                    );
                }
                linkedPassengers.add(passenger);
            }
        }

        // 5. Create Route with waypoints
        Route route = createRoute(createRideRequestDTO);
        routeRepository.save(route);

        // 6. Calculate price
        double estimatedPrice = calculatePrice(route, createRideRequestDTO.getVehicleType());

        // 7. Parse vehicle type
        VehicleType vehicleType = parseVehicleType(createRideRequestDTO.getVehicleType());

        // 8. Create ActiveRide
        ActiveRide ride = new ActiveRide();
        ride.setRoute(route);
        ride.setScheduledTime(scheduledTime);
        ride.setEstimatedPrice(estimatedPrice);
        ride.setVehicleType(vehicleType);
        ride.setNeedsBabySeats(createRideRequestDTO.getHasBaby() != null && createRideRequestDTO.getHasBaby());
        ride.setNeedsPetFriendly(createRideRequestDTO.getHasPets() != null && createRideRequestDTO.getHasPets());
        ride.setPayingPassenger(payingPassenger);
        ride.setLinkedPassengers(linkedPassengers);
        ride.setCurrentLocation(route.getWaypoints().getFirst()); // Start at first waypoint

        // 9. Set status based on immediate vs scheduled
        if (scheduledTime != null) {
            ride.setStatus(RideStatus.SCHEDULED);
        } else {
            ride.setStatus(RideStatus.DRIVER_INCOMING);
        }

        // 10. Try to assign driver (only for immediate rides)
        if (scheduledTime == null) {
            Driver driver = driverService.findAvailableDriver(ride);

            if (driver == null) {
                return new CreatedRideResponseDTO(
                        "NO_DRIVERS_AVAILABLE",
                        "No drivers available at the moment",
                        null
                );
            }

            ride.setDriver(driver);
        }

        // 11. Save ride
        ActiveRide savedRide = activeRideRepository.save(ride);

        // 12. Return success
        return new CreatedRideResponseDTO(
                "SUCCESS",
                scheduledTime != null
                        ? "Ride scheduled successfully. Driver will be assigned closer to scheduled time."
                        : "Ride ordered successfully!",
                savedRide.getId()
        );
    }

    private LocalDateTime parseScheduledTime(String timeString) {
        try {
            LocalTime time = LocalTime.parse(timeString);
            LocalDateTime scheduled = LocalDateTime.of(LocalDateTime.now().toLocalDate(), time);

            if (scheduled.isBefore(LocalDateTime.now())) {
                scheduled = scheduled.plusDays(1);
            }

            return scheduled;
        } catch (Exception e) {
            return null;
        }
    }

    private Route createRoute(CreateRideRequestDTO request) {
        Route route = new Route();

        // Set starting and ending points
        route.setStartingPoint(request.getAddresses().getFirst());
        route.setEndingPoint(request.getAddresses().get(request.getLatitudes().size() - 1));

        // Create waypoints for all coordinates (including start and end point)
        List<WayPoint> waypoints = new ArrayList<>();
        for (int i = 0; i < request.getLatitudes().size(); i++) {
            WayPoint waypoint = new WayPoint();
            waypoint.setLatitude(request.getLatitudes().get(i));
            waypoint.setLongitude(request.getLongitudes().get(i));
            waypoint.setAddress(request.getAddresses().get(i));
            waypoint.setReachedAt(null);
            waypoints.add(waypoint);
        }
        route.setWaypoints(waypoints);

        // TODO: Call Google Maps API to get distance, time, and polyline OR get from front based on implementation
        route.setEstDistanceKm(10.0);
        route.setEstTimeMin(20.0);
        route.setEncodedPolyline("");

        return route;
    }

    private double calculatePrice(Route route, String vehicleTypeStr) {
        double basePrice = getBasePrice(vehicleTypeStr);
        return basePrice + (route.getEstDistanceKm() * 120);
    }

    private double getBasePrice(String vehicleTypeStr) {
        if (vehicleTypeStr == null || vehicleTypeStr.isEmpty()) {
            return 200;
        }

        // TODO: PULL FROM DATABASE BASE PRICE PER VEHICLE TYPE WHEN IMPLEMENTED
        return switch (vehicleTypeStr.toUpperCase()) {
            case "SUV" -> 300;
            case "VAN" -> 500;
            default -> 200;
        };
    }

    private VehicleType parseVehicleType(String vehicleTypeStr) {
        try {
            return VehicleType.valueOf(vehicleTypeStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    @Override
    public UpdatedRideDTO startRide(Long rideId) {
        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalStateException("Ride not found"));

        // Verify ride is in correct status
        if (ride.getStatus() != RideStatus.DRIVER_INCOMING) {
            throw new IllegalStateException("Ride is not in DRIVER_INCOMING status");
        }

        // Start the ride
        ride.setActualStartTime(LocalDateTime.now());
        ride.setStatus(RideStatus.ACTIVE);

        activeRideRepository.save(ride);

        UpdatedRideDTO response = new UpdatedRideDTO();
        response.setId(ride.getId());
        response.setStatus("ACTIVE");
        response.setStartTime(ride.getActualStartTime());

        return response;
    }

    @Override
    public GetDriverActiveRideDTO getDriverActiveRide(String driverEmail) {
        Driver driver = driverRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Find ride that's waiting for driver to start
        ActiveRide ride = activeRideRepository
                .findByDriverAndStatus(driver, RideStatus.DRIVER_INCOMING)
                .orElse(null);
        if (ride == null) {
            return null;
        }

        GetDriverActiveRideDTO dto = new GetDriverActiveRideDTO();
        dto.setRideId(ride.getId());
        dto.setStartingPoint(ride.getRoute().getStartingPoint());
        dto.setEndingPoint(ride.getRoute().getEndingPoint());
        dto.setEstimatedPrice(ride.getEstimatedPrice());
        dto.setEstimatedTimeMin(ride.getRoute().getEstTimeMin());
        dto.setPassengerName(ride.getPayingPassenger().getName() + " " + ride.getPayingPassenger().getSurname());
        dto.setPassengerCount(1 + (ride.getLinkedPassengers() != null ? ride.getLinkedPassengers().size() : 0));

        return dto;
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

    @Override
    public UpdatedRideDTO finishRide(Long rideId, UpdateRideDTO rideRequest) {
        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalStateException("Ride not found"));

        if (ride.getStatus() != RideStatus.ACTIVE) {
            throw new IllegalStateException("Ride is not ACTIVE and cannot be finished");
        }

        // Create CompletedRide
        CompletedRide completedRide = new CompletedRide();
        completedRide.setRoute(ride.getRoute());
        completedRide.setScheduledTime(ride.getScheduledTime());
        completedRide.setStartTime(ride.getActualStartTime());
        completedRide.setEndTime(LocalDateTime.now());
        completedRide.setEstimatedPrice(ride.getEstimatedPrice());
        completedRide.setVehicleType(ride.getVehicleType());
        completedRide.setDriverId(ride.getDriver() != null ? ride.getDriver().getId() : null);
        completedRide.setDriverName(ride.getDriver() != null ? ride.getDriver().getName() : null);
        completedRide.setDriverEmail(ride.getDriver() != null ? ride.getDriver().getEmail() : null);
        completedRide.setPayingPassengerId(ride.getPayingPassenger().getId());
        completedRide.setPayingPassengerName(ride.getPayingPassenger().getName() + " " + ride.getPayingPassenger().getSurname());
        completedRide.setPayingPassengerEmail(ride.getPayingPassenger().getEmail());
        completedRide.setLinkedPassengerIds(
                ride.getLinkedPassengers() != null
                        ? ride.getLinkedPassengers().stream().map(Passenger::getId).toList()
                        : List.of()
        );
        completedRide.setCompletedNormally(true);
        completedRide.setCancelled(false);
        completedRide.setStoppedEarly(false);
        completedRide.setPanicPressed(false);

        // Save completed ride
        completedRide = completedRideRepository.save(completedRide);

        // Release or prepare the driver
        Driver driver = ride.getDriver();
        if (driver != null) {
            // Check if the driver has any scheduled rides
            Optional<ActiveRide> nextRideOpt = activeRideRepository
                    .findFirstByDriverAndStatusOrderByScheduledTimeAsc(driver, RideStatus.SCHEDULED);

            if (nextRideOpt.isPresent()) {
                // Driver has a scheduled ride → mark as busy (not available)
                driver.setActive(false);
                // TODO: here we could send the next ride data to the frontend
            } else {
                // No scheduled rides → driver is available for new rides
                driver.setActive(true);
            }
            driverRepository.save(driver);
        }


        // Send email to paying passenger
        emailService.sendRideFinishedEmail(
                ride.getPayingPassenger().getEmail(),
                ride.getPayingPassenger().getName(),
                completedRide.getId(),
                ride.getPayingPassenger().getId()
        );

        // Send email to linked passengers
        if (ride.getLinkedPassengers() != null) {
            for (Passenger p : ride.getLinkedPassengers()) {
                emailService.sendRideFinishedEmail(
                        p.getEmail(),
                        p.getName(),
                        completedRide.getId(),
                        p.getId()
                );
            }
        }


        // Remove active ride
        activeRideRepository.delete(ride);

        // Return DTO
        UpdatedRideDTO response = new UpdatedRideDTO();
        response.setId(completedRide.getId());
        response.setStatus("FINISHED");
        response.setEndTime(completedRide.getEndTime());

        return response;
    }



}
