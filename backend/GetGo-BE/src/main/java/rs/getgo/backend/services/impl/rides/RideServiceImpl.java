package rs.getgo.backend.services.impl.rides;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.panic.PanicAlertDTO;
import rs.getgo.backend.dtos.ride.*;
import rs.getgo.backend.dtos.rideStatus.CreatedRideStatusDTO;
import rs.getgo.backend.mappers.RideMapper;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.NotificationType;
import rs.getgo.backend.model.enums.RideOrderStatus;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.*;
import rs.getgo.backend.services.*;
import rs.getgo.backend.utils.AuthUtils;

import java.time.LocalDateTime;
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
    private final MapboxRoutingService routingService;
    private final WebSocketController webSocketController;
    private final InconsistencyReportRepository reportRepository;
    private final PanicNotifierService panicNotifierService;
    private final RideMapper rideMapper;
    private final NotificationService notificationService;
    private final RidePriceRepository ridePriceRepository;

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
                           MapboxRoutingService mapboxRoutingService,
                           WebSocketController webSocketController,
                           CompletedRideRepository completedRideRepository,
                           EmailService emailService,
                           InconsistencyReportRepository reportRepository,
                           PanicNotifierService panicNotifierService,
                           RideMapper rideMapper,
                           NotificationService notificationService,
                           RidePriceRepository ridePriceRepository) {
        this.cancellationRepository = cancellationRepository;
        this.panicRepository = panicRepository;
        this.activeRideRepository = activeRideRepository;
        this.userRepository = userRepository;
        this.passengerRepository = passengerRepository;
        this.routeRepository = routeRepository;
        this.driverRepository = driverRepository;
        this.completedRideRepository = completedRideRepository;
        this.emailService = emailService;
        this.routingService = mapboxRoutingService;
        this.webSocketController = webSocketController;
        this.reportRepository = reportRepository;
        this.panicNotifierService = panicNotifierService;
        this.rideMapper = rideMapper;
        this.notificationService = notificationService;
        this.ridePriceRepository = ridePriceRepository;
    }

    @Override
    public Notification cancelRide(ActiveRide ride, CancelRideDTO req) {
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

        // Create CompletedRide with cancelled status
        CompletedRide completedRide = new CompletedRide();
        completedRide.setRoute(ride.getRoute());
        completedRide.setScheduledTime(ride.getScheduledTime());
        completedRide.setStartTime(LocalDateTime.now());
        completedRide.setEndTime(LocalDateTime.now());
        completedRide.setEstimatedPrice(ride.getEstimatedPrice());
        completedRide.setEstDistanceKm(ride.getRoute().getEstDistanceKm());
        completedRide.setEstTime(ride.getRoute().getEstTimeMin());
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
        completedRide.setCancelled(true);
        completedRide.setCancelledByUserId(req.getCancelerId());
        completedRide.setCancelReason(req.getReason());
        completedRide.setStoppedEarly(false);
        completedRide.setPanicPressed(false);

        // Save completed ride first
        completedRide = completedRideRepository.save(completedRide);

        // Set notification message so passengers/drivers can read it via Notification entity
        String cancelledBy = "DRIVER".equals(role) ? "Driver" : "Passenger";
        // Short, clear notification without ride id
        String notifMsg = String.format("%s canceled the ride. Reason: %s", cancelledBy, req.getReason() != null ? req.getReason() : "No reason provided");

        // Create notifications to passenger and driver with the message and return the notification for caller
        Driver driver = ride.getDriver();
        rs.getgo.backend.model.entities.Notification passengerNotif = notificationService.createAndNotify(ride.getPayingPassenger().getId(), rs.getgo.backend.model.enums.NotificationType.RIDE_CANCELLED, "Ride canceled", notifMsg, LocalDateTime.now());
        if (driver != null) {
            notificationService.createAndNotify(driver.getId(), rs.getgo.backend.model.enums.NotificationType.RIDE_CANCELLED, "Ride canceled", notifMsg, LocalDateTime.now());
        }

        // Link panic records to completed ride if any exist
        List<Panic> ridePanics = panicRepository.findAll().stream()
                .filter(p -> p.getRideId() != null && p.getRideId().equals(ride.getId()))
                .collect(Collectors.toList());

        if (!ridePanics.isEmpty()) {
            for (Panic panic : ridePanics) {
                panic.setRideId(completedRide.getId());
                panicRepository.save(panic);
            }
            completedRide.setPanicPressed(true);
            completedRideRepository.save(completedRide);
        }

        // Link inconsistency reports to completed ride
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

        for (Passenger p : allPassengers) {
            notificationService.createAndNotify(p.getId(), rs.getgo.backend.model.enums.NotificationType.RIDE_CANCELLED, "Ride canceled", notifMsg, LocalDateTime.now());
        }

        // Release driver if assigned
        if (driver != null) {
            driver.setActive(true);
            driverRepository.save(driver);
        }

        activeRideRepository.delete(ride);

        return passengerNotif;
    }

    // low‑level helper already existing – leave as is
    @Override
    public Notification cancelRideByDriver(Long rideId, String reason) {
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

        return cancelRide(ride, dto);
    }

    @Override
    public Notification cancelRideByPassenger(Long rideId, String reason) {
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

        return cancelRide(ride, dto);
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

        notifyLinkedPassengersRideAccepted(ride);

        UpdatedRideDTO response = new UpdatedRideDTO();
        response.setId(ride.getId());
        response.setStatus(RideStatus.DRIVER_INCOMING.toString());
        response.setStartTime(null);

        return response;
    }

    private void notifyLinkedPassengersRideAccepted(ActiveRide ride) {
        if (ride.getLinkedPassengers() == null || ride.getLinkedPassengers().isEmpty() || ride.getDriver() == null) {
            return;
        }

        for (Passenger p : ride.getLinkedPassengers()) {
            if (!p.equals(ride.getPayingPassenger())) {
                emailService.sendLinkedPassengerEmail(p, ride);

//                pushNotificationService.sendNotification(
//                        p.getId(),
//                        "You have been added to a ride and the driver has accepted it!"
//                );
                notificationService.createAndNotify(
                        p.getId(),
                        NotificationType.RIDE_ACCEPTED,
                        "Ride accepted",
                        "You have been added to a ride. The driver has accepted it!",
                        LocalDateTime.now()
                );

                webSocketController.notifyPassengerLinkedRideAccepted(
                        p.getId(),
                        ride.getId(),
                        ride.getDriver().getName()
                );
            }
        }
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

        String pathJson = routingService.convertCoordinatesToJson(route.coordinates());

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
            GetDriverActiveRideDTO rideDTO = rideMapper.toDriverActiveRideDTO(waitingRide);
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
        String pathJson = routingService.convertCoordinatesToJson(route.coordinates());
        ride.setMovementPathJson(pathJson);
        ride.setCurrentPathIndex(0);
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
                                RideStatus.ACTIVE,
                                RideStatus.DRIVER_ARRIVED_AT_DESTINATION)
                )
                .stream()
                .findFirst()
                .orElse(null);
        if (ride == null) return null;

        return rideMapper.toDriverActiveRideDTO(ride);
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

        // Existing WS notification
        webSocketController.notifyAdminsPanicTriggered(
                ride.getId(),
                userId,
                email,
                triggeredAt
        );

        // New unified notifier for admin chat stream
        PanicAlertDTO dto = new PanicAlertDTO();
        dto.setPanicId(panic.getId());
        dto.setRideId(ride.getId());
        dto.setDriverId(ride.getDriver().getId());
        dto.setTriggeredByUserId(userId);
        dto.setTriggeredAt(triggeredAt);
        dto.setStatus(false);
        panicNotifierService.notifyAdmins(dto);

        // Create Notification for driver and passenger(s)
        // Short panic notification without ride id
        String msg = "PANIC button pressed — immediate attention required";
        if (ride.getDriver() != null) {
            notificationService.createAndNotify(ride.getDriver().getId(), rs.getgo.backend.model.enums.NotificationType.PANIC_ALERT, "Panic triggered", msg, triggeredAt);
        }
        notificationService.createAndNotify(ride.getPayingPassenger().getId(), rs.getgo.backend.model.enums.NotificationType.PANIC_ALERT, "Panic triggered", msg, triggeredAt);
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
        if (ride.getRoute() != null) {
            completedRide.setEstDistanceKm(ride.getRoute().getEstDistanceKm());
            completedRide.setEstTime(ride.getRoute().getEstTimeMin());
        } else {
            completedRide.setEstDistanceKm(0.0);
            completedRide.setEstTime(0.0);
        }
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
                completedRide.getEndTime(),
                completedRide.getDriverId()
        );

        // === WS: notify PASSENGERS ===
        webSocketController.notifyPassengerRideFinished(
                ride.getId(),
                completedRide.getEstimatedPrice(),
                completedRide.getStartTime(),
                completedRide.getEndTime(),
                completedRide.getDriverId()
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
                completedRide.setPanicPressed(true);
                completedRideRepository.save(completedRide);
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

        // If stop coordinates provided, update route's ending point and completed ride route
        if (stopRideDTO != null) {
            double lat = stopRideDTO.getLatitude();
            double lng = stopRideDTO.getLongitude();
            // Update currentLocation and route end
            ride.setCurrentLocation(new rs.getgo.backend.model.entities.WayPoint() {{ setLatitude(lat); setLongitude(lng); setAddress(null); }});
            // Update route ending point string
            if (ride.getRoute() != null) {
                ride.getRoute().setEndingPoint("Stopped location");
                // also update last waypoint coordinates if waypoints exist
                List<rs.getgo.backend.model.entities.WayPoint> wps = ride.getRoute().getWaypoints();
                if (wps != null && !wps.isEmpty()) {
                    rs.getgo.backend.model.entities.WayPoint last = wps.get(wps.size() - 1);
                    last.setLatitude(lat);
                    last.setLongitude(lng);
                    last.setAddress("Stopped location");
                }
                routeRepository.save(ride.getRoute());
            }
        }

        CompletedRide completedRide = new CompletedRide();
        completedRide.setRoute(ride.getRoute());
        completedRide.setScheduledTime(ride.getScheduledTime());
        completedRide.setStartTime(startTime);
        completedRide.setEndTime(endTime);
        completedRide.setEstimatedPrice(ride.getEstimatedPrice());
        if (ride.getRoute() != null) {
            completedRide.setEstDistanceKm(ride.getRoute().getEstDistanceKm());
            completedRide.setEstTime(ride.getRoute().getEstTimeMin());
        } else {
            completedRide.setEstDistanceKm(0.0);
            completedRide.setEstTime(0.0);
        }
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
                endTime,
                ride.getDriver().getId()
        );

        // Create notifications to passenger and driver
        // Short stop notification without ride id
        String stopMsg = "Ride stopped early at the provided location.";
        notificationService.createAndNotify(ride.getPayingPassenger().getId(), rs.getgo.backend.model.enums.NotificationType.RIDE_CANCELLED, "Ride stopped early", stopMsg, endTime);
        if (ride.getDriver() != null) {
            notificationService.createAndNotify(ride.getDriver().getId(), rs.getgo.backend.model.enums.NotificationType.RIDE_CANCELLED, "Ride stopped early", stopMsg, endTime);
        }

        RideCompletionDTO response = new RideCompletionDTO();
        response.setRideId(completedRide.getId());
        response.setStatus("STOPPED_EARLY");
        response.setPrice(actualPrice);
        response.setStartTime(startTime);
        response.setEndTime(endTime);
        response.setDurationMinutes(durationMinutes);
        response.setNotificationMessage(stopMsg);

        return response;
    }

    private double calculateStoppedRidePrice(ActiveRide ride, long durationMinutes) {
        // Business logic: charge proportional price based on time driven
        double estimatedPrice = ride.getEstimatedPrice();
        double estimatedDuration = 0.0;
        if (ride.getRoute() != null) {
            estimatedDuration = ride.getRoute().getEstTimeMin();
        }

        if (estimatedDuration <= 0) {
            return estimatedPrice; // fallback to estimated price
        }

        // Proportional price
        double proportionalPrice = (durationMinutes / estimatedDuration) * estimatedPrice;

        // Minimum charge (e.g., at least 50% of estimated price)
        double minCharge = estimatedPrice * 0.5;

        return Math.max(proportionalPrice, minCharge);
    }

}
