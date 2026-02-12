package rs.getgo.backend.services.impl.rides;

import org.springframework.stereotype.Service;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.ride.CreateRideRequestDTO;
import rs.getgo.backend.dtos.ride.CreatedRideResponseDTO;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.mappers.RideMapper;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RideOrderStatus;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.*;
import rs.getgo.backend.services.DriverMatchingService;
import rs.getgo.backend.services.RideOrderService;
import rs.getgo.backend.services.RidePriceService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RideOrderServiceImpl implements RideOrderService {
    private final WebSocketController webSocketController;
    private final PassengerRepository passengerRepository;
    private final BlockNoteRepository blockNoteRepository;
    private final RouteRepository routeRepository;
    private final ActiveRideRepository activeRideRepository;
    private final RidePriceService ridePriceService;
    private final MapboxRoutingService routingService;
    private final DriverMatchingService driverMatchingService;
    private final RideMapper rideMapper;

    public RideOrderServiceImpl(
            WebSocketController webSocketController,
            PassengerRepository passengerRepository,
            BlockNoteRepository blockNoteRepository,
            RouteRepository routeRepository,
            ActiveRideRepository activeRideRepository,
            RidePriceService ridePriceService,
            MapboxRoutingService routingService,
            DriverMatchingService driverMatchingService,
            RideMapper rideMapper
    ) {
        this.webSocketController = webSocketController;
        this.passengerRepository = passengerRepository;
        this.blockNoteRepository = blockNoteRepository;
        this.routeRepository = routeRepository;
        this.activeRideRepository = activeRideRepository;
        this.ridePriceService = ridePriceService;
        this.routingService = routingService;
        this.driverMatchingService = driverMatchingService;
        this.rideMapper = rideMapper;
    }

    @Override
    public CreatedRideResponseDTO orderRide(CreateRideRequestDTO createRideRequestDTO, String userEmail) {
        CreatedRideResponseDTO coordinatesError = validateCoordinates(createRideRequestDTO);
        if (coordinatesError != null) return coordinatesError;

        Passenger payingPassenger = passengerRepository.findByEmail(userEmail).orElse(null);
        CreatedRideResponseDTO passengerError = validatePayingPassenger(payingPassenger);
        if (passengerError != null) return passengerError;

        LocalDateTime scheduledTime = null;
        if (createRideRequestDTO.getScheduledTime() != null && !createRideRequestDTO.getScheduledTime().isEmpty()) {
            scheduledTime = parseScheduledTime(createRideRequestDTO.getScheduledTime());
            CreatedRideResponseDTO timeError = validateScheduledTime(scheduledTime);
            if (timeError != null) return timeError;
        }

        List<Passenger> linkedPassengers = collectLinkedPassengers(createRideRequestDTO.getFriendEmails());
        CreatedRideResponseDTO linkedError = validateLinkedPassengers(
                createRideRequestDTO.getFriendEmails(), linkedPassengers
        );
        if (linkedError != null) return linkedError;

        Route route = createRoute(createRideRequestDTO);
        routeRepository.save(route);

        VehicleType requestedVehicleType = parseVehicleType(createRideRequestDTO.getVehicleType());

        ActiveRide ride = buildActiveRide(createRideRequestDTO, route,
                scheduledTime, payingPassenger, linkedPassengers);

        if (scheduledTime == null) {
            CreatedRideResponseDTO driverError = assignDriverForImmediateRide(ride, route);
            if (driverError != null) return driverError;
        } else {
            ride.setVehicleType(requestedVehicleType);
            ride.setStatus(RideStatus.SCHEDULED);
        }

        ActiveRide savedRide = activeRideRepository.save(ride);
        notifyDriverIfReady(savedRide);

        return new CreatedRideResponseDTO(
                "SUCCESS",
                scheduledTime != null
                        ? "Ride scheduled successfully. Driver will be assigned closer to scheduled time."
                        : "Ride ordered successfully!",
                savedRide.getId()
        );
    }

    private CreatedRideResponseDTO validateCoordinates(CreateRideRequestDTO request) {
        if (request.getLatitudes().size() < 2 ||
                request.getLatitudes().size() != request.getLongitudes().size() ||
                request.getLatitudes().size() != request.getAddresses().size()) {
            return new CreatedRideResponseDTO(
                    "INVALID_REQUEST",
                    "Invalid coordinates or addresses",
                    null
            );
        }
        return null;
    }

    private CreatedRideResponseDTO validatePayingPassenger(Passenger payingPassenger) {
        if (payingPassenger == null) {
            return new CreatedRideResponseDTO(
                    RideOrderStatus.PASSENGER_NOT_FOUND.toString(),
                    "Passenger account not found",
                    null
            );
        }

        if (payingPassenger.isBlocked()) {
            String reason = blockNoteRepository.findByUserAndUnblockedAtIsNull(payingPassenger)
                    .map(BlockNote::getReason)
                    .orElse("You have been blocked.");
            return new CreatedRideResponseDTO(
                    "blocked",
                    "Cannot order ride: user is blocked. Reason: " + reason,
                    null
            );
        }

        // Check for any active ride that's not far-future scheduled
        boolean hasBlockingRide = activeRideRepository.existsByPayingPassengerAndStatusNot(
                payingPassenger, RideStatus.SCHEDULED
        ) || activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(
                payingPassenger, RideStatus.SCHEDULED
        );

        // Cannot start ride if scheduled ride starts in less than 1h
        LocalDateTime soonThreshold = LocalDateTime.now().plusHours(1);
        boolean hasUpcomingScheduled = activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                payingPassenger, RideStatus.SCHEDULED, soonThreshold
        ) || activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                payingPassenger, RideStatus.SCHEDULED, soonThreshold
        );

        if (hasBlockingRide || hasUpcomingScheduled) {
            return new CreatedRideResponseDTO(
                    "PASSENGER_HAS_ACTIVE_RIDE",
                    "You already have an active or upcoming ride",
                    null
            );
        }

        return null;
    }

    private CreatedRideResponseDTO validateScheduledTime(LocalDateTime scheduledTime) {
        if (scheduledTime == null ||
                scheduledTime.isBefore(LocalDateTime.now()) ||
                scheduledTime.isAfter(LocalDateTime.now().plusHours(5))) {
            return new CreatedRideResponseDTO(
                    RideOrderStatus.INVALID_SCHEDULED_TIME.toString(),
                    "Scheduled time must be within the next 5 hours",
                    null
            );
        }
        return null;
    }

    private CreatedRideResponseDTO validateLinkedPassengers(
            List<String> friendEmails,
            List<Passenger> linkedPassengers
    ) {
        if (friendEmails == null) return null;

        LocalDateTime soonThreshold = LocalDateTime.now().plusMinutes(30);

        for (Passenger passenger : linkedPassengers) {
            boolean hasBlockingRide = activeRideRepository.existsByPayingPassengerAndStatusNot(
                    passenger, RideStatus.SCHEDULED
            ) || activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(
                    passenger, RideStatus.SCHEDULED
            );

            boolean hasUpcomingScheduled = activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                    passenger, RideStatus.SCHEDULED, soonThreshold
            ) || activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                    passenger, RideStatus.SCHEDULED, soonThreshold
            );

            if (hasBlockingRide || hasUpcomingScheduled) {
                return new CreatedRideResponseDTO(
                        "LINKED_PASSENGER_HAS_ACTIVE_RIDE",
                        "Passenger " + passenger.getEmail() + " already has an active or upcoming ride",
                        null
                );
            }
        }

        return null;
    }

    private List<Passenger> collectLinkedPassengers(List<String> friendEmails) {
        List<Passenger> linkedPassengers = new ArrayList<>();
        if (friendEmails == null) return linkedPassengers;

        for (String email : friendEmails) {
            passengerRepository.findByEmail(email).ifPresent(linkedPassengers::add);
        }

        return linkedPassengers;
    }

    private ActiveRide buildActiveRide(
            CreateRideRequestDTO request,
            Route route,
            LocalDateTime scheduledTime,
            Passenger payingPassenger,
            List<Passenger> linkedPassengers
    ) {
        ActiveRide ride = new ActiveRide();
        ride.setRoute(route);
        ride.setScheduledTime(scheduledTime);
        ride.setNeedsBabySeats(request.getHasBaby() != null && request.getHasBaby());
        ride.setNeedsPetFriendly(request.getHasPets() != null && request.getHasPets());
        ride.setPayingPassenger(payingPassenger);
        ride.setLinkedPassengers(linkedPassengers);
        ride.setCurrentLocation(route.getWaypoints().getFirst());
        return ride;
    }

    private CreatedRideResponseDTO assignDriverForImmediateRide(ActiveRide ride, Route route) {
        Driver driver = driverMatchingService.findAvailableDriver(ride);

        if (driver == null) {
            return new CreatedRideResponseDTO(
                    "NO_DRIVERS_AVAILABLE",
                    "No drivers available at the moment",
                    null
            );
        }

        ride.setDriver(driver);

        VehicleType actualVehicleType = driver.getVehicle().getType();
        ride.setVehicleType(actualVehicleType);
        ride.setEstimatedPrice(ridePriceService.calculateRidePrice(actualVehicleType, route.getEstDistanceKm()));

        if (activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE)) {
            ride.setStatus(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE);
        } else {
            ride.setStatus(RideStatus.DRIVER_READY);
        }

        return null;
    }

    private void notifyDriverIfReady(ActiveRide savedRide) {
        if (savedRide.getStatus() == RideStatus.DRIVER_READY) {
            GetDriverActiveRideDTO rideDTO = rideMapper.toDriverActiveRideDTO(savedRide);
            webSocketController.notifyDriverRideAssigned(savedRide.getDriver().getEmail(), rideDTO);
        }
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

        route.setStartingPoint(request.getAddresses().getFirst());
        route.setEndingPoint(request.getAddresses().get(request.getLatitudes().size() - 1));

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
        List<MapboxRoutingService.Coordinate> allCoordinates = new ArrayList<>();

        for (int i = 0; i < waypoints.size() - 1; i++) {
            WayPoint from = waypoints.get(i);
            WayPoint to = waypoints.get(i + 1);

            MapboxRoutingService.RouteResponse segment = routingService.getRoute(
                    from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude()
            );

            totalDistance += segment.distanceKm();
            totalTime += segment.realDurationSeconds() / 60.0;

            if (i == 0) {
                allCoordinates.addAll(segment.coordinates());
            } else {
                allCoordinates.addAll(segment.coordinates().subList(1, segment.coordinates().size()));
            }
        }

        route.setEstDistanceKm(totalDistance);
        route.setEstTimeMin(totalTime);

        String polylineJson = routingService.convertCoordinatesToJson(allCoordinates);
        route.setEncodedPolyline(polylineJson);

        return route;
    }

    private VehicleType parseVehicleType(String vehicleTypeStr) {
        if (vehicleTypeStr == null || vehicleTypeStr.trim().isEmpty()) {
            return null;
        }

        try {
            return VehicleType.valueOf(vehicleTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}