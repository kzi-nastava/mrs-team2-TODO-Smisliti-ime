package rs.getgo.backend.services.impl.rides;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.driver.GetDriverLocationDTO;
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
import rs.getgo.backend.utils.AuthUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RideServiceImpl implements RideService {

    private final RideCancellationRepository cancellationRepository;
    private final PanicRepository panicRepository;
    private final ActiveRideRepository activeRideRepository;
    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final RouteRepository routeRepository;
    private final DriverRepository driverRepository;
    private final CompletedRideRepository completedRideRepository;
    private final EmailService emailService;
    private final DriverService driverService;
    private final MapboxRoutingService routingService;
    private final WebSocketController webSocketController;
    private final InconsistencyReportRepository reportRepository;

    @Value("${driver.default.latitude}")
    private Double defaultDriverLatitude;

    @Value("${driver.default.longitude}")
    private Double defaultDriverLongitude;

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
                           MapboxRoutingService mapboxRoutingService,
                           WebSocketController webSocketController,
                           CompletedRideRepository completedRideRepository,
                           EmailService emailService,
                           InconsistencyReportRepository reportRepository) {
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
        this.routingService = mapboxRoutingService;
        this.webSocketController = webSocketController;
        this.reportRepository = reportRepository;
    }

    @Override
    public void cancelRide(ActiveRide ride, CancelRideDTO req) {
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
        rc.setRideId(ride.getId());
        rc.setCancelerId(req.getCancelerId());
        rc.setRole(role);
        rc.setReason(req.getReason());
        rc.setCreatedAt(LocalDateTime.now());
        cancellationRepository.save(rc);

        List<Panic> ridePanics = panicRepository.findAll().stream()
                .filter(p -> p.getRideId() != null && p.getRideId().equals(ride.getId()))
                .collect(Collectors.toList());
        if (!ridePanics.isEmpty()) {
            panicRepository.deleteAll(ridePanics);
        }

        activeRideRepository.delete(ride);

        new CreatedRideStatusDTO(ride.getId(), "CANCELED");
    }

    // low‑level helper already existing – leave as is
    @Override
    public void cancelRideByDriver(Long rideId, String reason) {
        String email = AuthUtils.getCurrentUserEmail();
        Long driverId = driverRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Driver not found"))
                .getId();

        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalStateException("Ride not found"));

        // Allowed statuses: DRIVER_READY, DRIVER_INCOMING (before pickup).
        // Disallow after ACTIVE.
        if (ride.getStatus() == RideStatus.ACTIVE) {
            throw new IllegalStateException("Driver cannot cancel after ride started");
        }

        Driver oldDriver = ride.getDriver();
        if (oldDriver == null || !oldDriver.getId().equals(driverId)) {
            throw new IllegalStateException("Only assigned driver can cancel this ride");
        }

        // Log cancellation for this driver
        CancelRideDTO dto = new CancelRideDTO();
        dto.setRole("DRIVER");
        dto.setReason(reason);
        dto.setCancelerId(driverId);
        dto.setPassengersEntered(false);
        dto.setScheduledStartTime(ride.getScheduledTime());
        cancelRide(ride, dto);
    }

    @Override
    public void cancelRideByPassenger(Long rideId, String reason) {
        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalStateException("Ride not found"));

        String email = AuthUtils.getCurrentUserEmail();
        Long passengerId = passengerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Passenger not found"))
                .getId();

        LocalDateTime scheduled = ride.getScheduledTime();
        if (scheduled == null) {
            throw new IllegalStateException("Ride is not scheduled and cannot be canceled this way");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = scheduled.minusMinutes(PASSENGER_CANCEL_MINUTES_BEFORE);
        if (!now.isBefore(cutoff)) {
            throw new IllegalStateException("Too late to cancel (must cancel at least 10 minutes before start)");
        }

        CancelRideDTO dto = new CancelRideDTO();
        dto.setRole("PASSENGER");
        dto.setReason(reason);
        dto.setCancelerId(passengerId);
        dto.setPassengersEntered(false);
        dto.setScheduledStartTime(scheduled);

        cancelRide(ride, dto);
    }

    @Override
    public CreatedRideResponseDTO orderRide(CreateRideRequestDTO createRideRequestDTO, String userEmail) {
        // Validate request
        if (createRideRequestDTO.getLatitudes().size() < 2 ||
                createRideRequestDTO.getLatitudes().size() != createRideRequestDTO.getLongitudes().size() ||
                createRideRequestDTO.getLatitudes().size() != createRideRequestDTO.getAddresses().size()) {
            return new CreatedRideResponseDTO(
                    "INVALID_REQUEST",
                    "Invalid coordinates or addresses",
                    null
            );
        }

        // Find paying passenger
        Passenger payingPassenger = passengerRepository.findByEmail(userEmail)
                .orElse(null);
        if (payingPassenger == null) {
            return new CreatedRideResponseDTO(
                    RideOrderStatus.PASSENGER_NOT_FOUND.toString(),
                    "Passenger account not found",
                    null
            );
        }

        // Parse scheduled time
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

        // Find linked passengers
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

        // Create Route with waypoints
        Route route = createRoute(createRideRequestDTO);
        routeRepository.save(route);

        // Calculate price
        double estimatedPrice = calculatePrice(route, createRideRequestDTO.getVehicleType());

        // Parse vehicle type
        VehicleType vehicleType = parseVehicleType(createRideRequestDTO.getVehicleType());

        // Create ActiveRide
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

        if (scheduledTime == null) {
            // Assign driver if ride is not scheduled and set according status
            Driver driver = driverService.findAvailableDriver(ride);

            if (driver == null) {
                return new CreatedRideResponseDTO(
                        "NO_DRIVERS_AVAILABLE",
                        "No drivers available at the moment",
                        null
                );
            }

            ride.setDriver(driver);

            // Decide initial status based on driver's current state
            if (activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE)) {
                ride.setStatus(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE);
            } else {
                ride.setStatus(RideStatus.DRIVER_READY);
            }
        } else {
            // Set status to scheduled and don't pick driver yet
            ride.setStatus(RideStatus.SCHEDULED);
        }

        // Save ride
        ActiveRide savedRide = activeRideRepository.save(ride);

        // Notify driver and passengers about assigned ride
        if (savedRide.getStatus() == RideStatus.DRIVER_READY) {
            GetDriverActiveRideDTO rideDTO = buildDriverActiveRideDTO(savedRide);
            webSocketController.notifyDriverRideAssigned(savedRide.getDriver().getEmail(), rideDTO);
        }
        // Note: there is no notifying passenger because passenger has separate order ride and track ride pages

        return new CreatedRideResponseDTO(
                "SUCCESS",
                scheduledTime != null
                        ? "Ride scheduled successfully. Driver will be assigned closer to scheduled time."
                        : "Ride ordered successfully!",
                savedRide.getId()
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

        double totalDistance = 0.0;
        double totalTime = 0.0;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            WayPoint from = waypoints.get(i);
            WayPoint to = waypoints.get(i + 1);

            MapboxRoutingService.RouteResponse segment = routingService.getRoute(
                    from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude()
            );

            totalDistance += segment.distanceKm();
            totalTime += segment.realDurationSeconds() / 60.0;
        }

        route.setEstDistanceKm(totalDistance); // Distance from start to end point
        route.setEstTimeMin(totalTime); // Duration from start to end point
        route.setEncodedPolyline(""); // TODO: remove field or use this instead of movementPathJson in ActiveRide

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
    public UpdatedRideDTO acceptRide(Long rideId) {
        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalStateException("Ride not found"));

        if (ride.getStatus() != RideStatus.DRIVER_READY) {
            throw new IllegalStateException("Ride is not in DRIVER_READY status");
        }

        // Change status and initialize movement
        ride.setStatus(RideStatus.DRIVER_INCOMING);
        initializeDriverToPickupMovement(ride);
        activeRideRepository.save(ride);

        // Notify driver
        webSocketController.notifyDriverStatusUpdate(
                ride.getDriver().getEmail(),
                ride.getId(),
                RideStatus.DRIVER_INCOMING.toString()
        );
        // Notify passenger
        webSocketController.notifyPassengerRideStatusUpdate(
                ride.getId(),
                RideStatus.DRIVER_INCOMING.toString(),
                "Driver is on the way to pick you up!"
        );

        UpdatedRideDTO response = new UpdatedRideDTO();
        response.setId(ride.getId());
        response.setStatus(RideStatus.DRIVER_INCOMING.toString());
        response.setStartTime(null);

        return response;
    }

    private void initializeDriverToPickupMovement(ActiveRide ride) {
        Driver driver = ride.getDriver();
        WayPoint pickupPoint = ride.getRoute().getWaypoints().getFirst();
        Double driverLat = driver.getCurrentLatitude();
        Double driverLng = driver.getCurrentLongitude();

        if (driverLat == null || driverLng == null) {
            driverLat = defaultDriverLatitude;
            driverLng = defaultDriverLongitude;
            driver.setCurrentLatitude(driverLat);
            driver.setCurrentLongitude(driverLng);
            driver.setLastLocationUpdate(LocalDateTime.now());
            driverRepository.save(driver);
        }

        MapboxRoutingService.RouteResponse route = routingService.getRoute(
                driverLat, driverLng,
                pickupPoint.getLatitude(), pickupPoint.getLongitude()
        );

        String pathJson = convertCoordinatesToJson(route.coordinates());

        ride.setMovementPathJson(pathJson); // Set movement to: driver location -> start point
        ride.setCurrentPathIndex(0);
        ride.setTargetWaypointIndex(0);
    }

    @Override
    public void handleWaypointReached(ActiveRide ride) {
        if (ride.getStatus() == RideStatus.DRIVER_INCOMING) {
            handleDriverArrivedAtPickup(ride);
        } else if (ride.getStatus() == RideStatus.ACTIVE) {
            handleDriverArrivedAtRideWaypoint(ride);
        }
    }

    private void handleDriverArrivedAtPickup(ActiveRide ride) {
        // Mark pickup point as reached
        WayPoint pickupPoint = ride.getRoute().getWaypoints().getFirst();
        pickupPoint.setReachedAt(LocalDateTime.now());

        routeRepository.save(ride.getRoute());

        ride.setStatus(RideStatus.DRIVER_ARRIVED);
        ride.setMovementPathJson(null);
        ride.setCurrentPathIndex(0);

        activeRideRepository.save(ride);

        // Notify driver
        webSocketController.notifyDriverStatusUpdate(
                ride.getDriver().getEmail(),
                ride.getId(),
                RideStatus.DRIVER_ARRIVED.toString()
        );
        // Notify passenger
        webSocketController.notifyPassengerRideStatusUpdate(
                ride.getId(),
                RideStatus.DRIVER_ARRIVED.toString(),
                "Driver has arrived at pickup location!"
        );
    }

    private boolean isLastWaypointReached(ActiveRide ride) {
        return ride.getTargetWaypointIndex() >= ride.getRoute().getWaypoints().size() - 1;
    }

    private void handleDriverArrivedAtRideWaypoint(ActiveRide ride) {
        Integer targetIndex = ride.getTargetWaypointIndex();
        List<WayPoint> waypoints = ride.getRoute().getWaypoints();

        // Mark current waypoint as reached
        WayPoint currentWaypoint = waypoints.get(targetIndex);
        currentWaypoint.setReachedAt(LocalDateTime.now());

        routeRepository.save(ride.getRoute());

        // Check if there are more waypoints
        if (!isLastWaypointReached(ride)) {
            // Move to next waypoint
            ride.setTargetWaypointIndex(targetIndex + 1);
            generateNextSegmentPath(ride);
            activeRideRepository.save(ride);
        } else {
            ride.setStatus(RideStatus.DRIVER_ARRIVED_AT_DESTINATION);
            activeRideRepository.save(ride);

            // Notify driver and passenger
            webSocketController.notifyDriverStatusUpdate(
                    ride.getDriver().getEmail(),
                    ride.getId(),
                    RideStatus.DRIVER_ARRIVED_AT_DESTINATION.toString()
            );
            webSocketController.notifyPassengerRideStatusUpdate(
                    ride.getId(),
                    RideStatus.DRIVER_ARRIVED_AT_DESTINATION.toString(),
                    "Driver has arrived at the destination!"
            );

        }
    }

    // Activates scheduled ride if driver has any
    private void activateWaitingRideForDriver(Driver driver) {
        ActiveRide waitingRide = activeRideRepository
                .findByDriverAndStatus(driver, RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE)
                .orElse(null);

        if (waitingRide != null) {
            waitingRide.setStatus(RideStatus.DRIVER_READY);
            activeRideRepository.save(waitingRide);

            // Notify driver about next ride
            GetDriverActiveRideDTO rideDTO = buildDriverActiveRideDTO(waitingRide);
            webSocketController.notifyDriverRideAssigned(driver.getEmail(), rideDTO);
            // Notify passenger
            webSocketController.notifyPassengerRideStatusUpdate(
                    waitingRide.getId(),
                    RideStatus.DRIVER_READY.toString(),
                    "Driver finished previous ride! Waiting for driver to accept ride..."
            );
        }
    }

    // Starts a ride if driver's arrived at pickup point for it
    @Override
    public UpdatedRideDTO startRide(Long rideId) {
        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalStateException("Ride not found"));

        // Verify ride is in correct status
        if (ride.getStatus() != RideStatus.DRIVER_ARRIVED) {
            throw new IllegalStateException("Driver must be at pickup to start ride");
        }

        // Start the ride
        ride.setActualStartTime(LocalDateTime.now());
        ride.setStatus(RideStatus.ACTIVE);
        ride.setTargetWaypointIndex(1); // Next target is first destination

        // Generate path from pickup to first destination
        generateNextSegmentPath(ride);

        activeRideRepository.save(ride);

        // Notify passengers
        webSocketController.notifyPassengerRideStatusUpdate(
                ride.getId(),
                RideStatus.ACTIVE.toString(),
                "Ride started!"
        );

        UpdatedRideDTO response = new UpdatedRideDTO();
        response.setId(ride.getId());
        response.setStatus("ACTIVE");
        response.setStartTime(ride.getActualStartTime());

        return response;
    }

    private void generateNextSegmentPath(ActiveRide ride) {
        List<WayPoint> waypoints = ride.getRoute().getWaypoints();
        Integer currentTarget = ride.getTargetWaypointIndex();
        Integer previousTarget = currentTarget - 1;

        WayPoint from = waypoints.get(previousTarget);
        WayPoint to = waypoints.get(currentTarget);

        MapboxRoutingService.RouteResponse route = routingService.getRoute(
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude()
        );

        // Set movement of active ride to waypoint(i) -> waypoint(j) where waypoint(0) is start point and (n-1) is dest.
        String pathJson = convertCoordinatesToJson(route.coordinates());
        ride.setMovementPathJson(pathJson);
        ride.setCurrentPathIndex(0);
    }

    private String convertCoordinatesToJson(List<MapboxRoutingService.Coordinate> coordinates) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < coordinates.size(); i++) {
            MapboxRoutingService.Coordinate coord = coordinates.get(i);
            json.append(String.format("[%.6f,%.6f]", coord.longitude(), coord.latitude()));
            if (i < coordinates.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    public GetDriverActiveRideDTO getDriverActiveRide(String driverEmail) {
        Driver driver = driverRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        ActiveRide ride = activeRideRepository
                .findByDriverAndStatusIn(
                        driver,
                        List.of(RideStatus.DRIVER_READY,
                                RideStatus.DRIVER_INCOMING,
                                RideStatus.DRIVER_ARRIVED,
                                RideStatus.ACTIVE)
                )
                .stream()
                .findFirst()
                .orElse(null);
        if (ride == null) return null;

        return buildDriverActiveRideDTO(ride);
    }

    @Override
    public void triggerPanic(Long rideId, String email) {

        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride not found"));

        Panic panic = new Panic();
        panic.setRideId(ride.getId());
        Long userId = userRepository.findIdByEmail(email);
        panic.setTriggeredByUserId(userId);
        LocalDateTime triggeredAt = LocalDateTime.now();
        panic.setTriggeredAt(triggeredAt);

        panicRepository.save(panic);

        webSocketController.notifyAdminsPanicTriggered(
                ride.getId(),
                userId,
                email,
                triggeredAt
        );
    }

    @Override
    public UpdatedRideDTO finishRide(Long rideId, UpdateRideDTO rideRequest) {
        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalStateException("Ride not found"));

        if (ride.getStatus() != RideStatus.ACTIVE &&
                ride.getStatus() != RideStatus.DRIVER_ARRIVED_AT_DESTINATION) {
            throw new IllegalStateException("Ride cannot be finished in current state");
        }


        // Create CompletedRide
        CompletedRide completedRide = new CompletedRide();
        completedRide.setRoute(ride.getRoute());
        completedRide.setScheduledTime(ride.getScheduledTime());
        completedRide.setStartTime(ride.getActualStartTime());
        completedRide.setEndTime(LocalDateTime.now());
        completedRide.setEstimatedPrice(ride.getEstimatedPrice());
        completedRide.setVehicleType(ride.getVehicleType());
        completedRide.setNeedsBabySeats(ride.isNeedsBabySeats());
        completedRide.setNeedsPetFriendly(ride.isNeedsPetFriendly());
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

        List<Passenger> allPassengers = new ArrayList<>();
        allPassengers.add(ride.getPayingPassenger());
        if (ride.getLinkedPassengers() != null) {
            allPassengers.addAll(ride.getLinkedPassengers());
        }

        for (Passenger p : allPassengers) {
            List<InconsistencyReport> reports = reportRepository.findUnlinkedReportsByPassenger(p);
            for (InconsistencyReport report : reports) {
                report.setCompletedRide(completedRide);
                reportRepository.save(report);
            }
        }

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

        // === WS: notify DRIVER ===
        webSocketController.notifyDriverRideFinished(
                ride.getDriver().getEmail(),
                ride.getId(),
                completedRide.getEstimatedPrice(),
                completedRide.getStartTime(),
                completedRide.getEndTime()
        );

        // === WS: notify PASSENGERS ===
        webSocketController.notifyPassengerRideFinished(
                ride.getId(),
                completedRide.getEstimatedPrice(),
                completedRide.getStartTime(),
                completedRide.getEndTime()
        );

        // Return DTO
        UpdatedRideDTO response = new UpdatedRideDTO();
        response.setId(completedRide.getId());
        response.setStatus("FINISHED");
        response.setEndTime(completedRide.getEndTime());

        // When active ride is completed, panic gets completed rideId
        List<Optional<Panic>> panics = panicRepository.findByRideId(rideId);
        for (Optional<Panic> panic : panics) {
            if (panic.isPresent()) {
                panic.get().setRideId(completedRide.getId());
            }
        }

        activeRideRepository.delete(ride);

        if (driver != null) {
            activateWaitingRideForDriver(driver);
        }

        return response;
    }

    @Override
    public RideCompletionDTO stopRide(Long rideId, StopRideDTO stopRideDTO) {
        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalStateException("Ride not found"));

        if (ride.getStatus() != RideStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE rides can be stopped");
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = ride.getActualStartTime();
        long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        double actualPrice = calculateStoppedRidePrice(ride, durationMinutes);

        CompletedRide completedRide = new CompletedRide();
        completedRide.setRoute(ride.getRoute());
        completedRide.setScheduledTime(ride.getScheduledTime());
        completedRide.setStartTime(startTime);
        completedRide.setEndTime(endTime);
        completedRide.setEstimatedPrice(ride.getEstimatedPrice());
        completedRide.setVehicleType(ride.getVehicleType());
        completedRide.setNeedsBabySeats(ride.isNeedsBabySeats());
        completedRide.setNeedsPetFriendly(ride.isNeedsPetFriendly());
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
        completedRide.setCompletedNormally(false);
        completedRide.setCancelled(false);
        completedRide.setStoppedEarly(true);
        completedRide.setPanicPressed(false);

        completedRide = completedRideRepository.save(completedRide);

        // Link all reports of this passenger without CompletedRide to this completed ride
        List<Passenger> allPassengers = new ArrayList<>();
        allPassengers.add(ride.getPayingPassenger());
        if (ride.getLinkedPassengers() != null) {
            allPassengers.addAll(ride.getLinkedPassengers());
        }

        for (Passenger p : allPassengers) {
            List<InconsistencyReport> reports = reportRepository.findUnlinkedReportsByPassenger(p);
            for (InconsistencyReport report : reports) {
                report.setCompletedRide(completedRide);
                reportRepository.save(report);
            }
        }

        // Release driver
        Driver driver = ride.getDriver();
        if (driver != null) {
            driver.setActive(true);
            driverRepository.save(driver);
        }

        // Remove active ride
        activeRideRepository.delete(ride);

        List<Panic> ridePanics = panicRepository.findAll().stream()
                .filter(p -> p.getRideId() != null && p.getRideId().equals(ride.getId()))
                .collect(Collectors.toList());
        if (!ridePanics.isEmpty()) {
            panicRepository.deleteAll(ridePanics);
        }

        // NEW: WS notification to passenger
        webSocketController.notifyPassengerRideStoppedEarly(
                ride.getId(),
                actualPrice,
                startTime,
                endTime
        );

        RideCompletionDTO response = new RideCompletionDTO();
        response.setRideId(completedRide.getId());
        response.setStatus("STOPPED_EARLY");
        response.setPrice(actualPrice);
        response.setStartTime(startTime);
        response.setEndTime(endTime);
        response.setDurationMinutes(durationMinutes);

        return response;
    }

    private double calculateStoppedRidePrice(ActiveRide ride, long durationMinutes) {
        // Business logic: charge proportional price based on time driven
        double estimatedPrice = ride.getEstimatedPrice();
        double estimatedDuration = ride.getRoute().getEstTimeMin();

        if (estimatedDuration <= 0) {
            return estimatedPrice; // fallback
        }

        // Proportional price
        double proportionalPrice = (durationMinutes / estimatedDuration) * estimatedPrice;

        // Minimum charge (e.g., at least 50% of estimated price)
        double minCharge = estimatedPrice * 0.5;

        return Math.max(proportionalPrice, minCharge);
    }

}
