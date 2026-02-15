package rs.getgo.backend.services.impl.rides;

import org.springframework.stereotype.Service;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.ride.CreateRideRequestDTO;
import rs.getgo.backend.dtos.ride.CreatedRideResponseDTO;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.mappers.RideMapper;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.NotificationType;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.RouteRepository;
import rs.getgo.backend.services.DriverMatchingService;
import rs.getgo.backend.services.NotificationService;
import rs.getgo.backend.services.RideOrderService;
import rs.getgo.backend.services.RidePriceService;
import rs.getgo.backend.validators.RideOrderValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RideOrderServiceImpl implements RideOrderService {
    private final WebSocketController webSocketController;
    private final RouteRepository routeRepository;
    private final ActiveRideRepository activeRideRepository;
    private final RidePriceService ridePriceService;
    private final MapboxRoutingService routingService;
    private final DriverMatchingService driverMatchingService;
    private final NotificationService notificationService;
    private final RideMapper rideMapper;
    private final RideOrderValidator rideOrderValidator;

    public RideOrderServiceImpl(
            WebSocketController webSocketController,
            RouteRepository routeRepository,
            ActiveRideRepository activeRideRepository,
            RidePriceService ridePriceService,
            MapboxRoutingService routingService,
            DriverMatchingService driverMatchingService,
            NotificationService notificationService,
            RideMapper rideMapper,
            RideOrderValidator rideOrderValidator
    ) {
        this.webSocketController = webSocketController;
        this.routeRepository = routeRepository;
        this.activeRideRepository = activeRideRepository;
        this.ridePriceService = ridePriceService;
        this.routingService = routingService;
        this.driverMatchingService = driverMatchingService;
        this.notificationService = notificationService;
        this.rideMapper = rideMapper;
        this.rideOrderValidator = rideOrderValidator;
    }

    @Override
    public CreatedRideResponseDTO orderRide(CreateRideRequestDTO createRideRequestDTO, String userEmail) {
        RideOrderValidator.ValidationResult validationResult = rideOrderValidator.validateRideOrder(createRideRequestDTO, userEmail);
        if (!validationResult.isValid()) {
            return validationResult.error();
        }

        Route route = createRoute(createRideRequestDTO);
        routeRepository.save(route);

        VehicleType requestedVehicleType = parseVehicleType(createRideRequestDTO.getVehicleType());

        ActiveRide ride = buildActiveRide(
                createRideRequestDTO,
                route,
                requestedVehicleType,
                validationResult.scheduledTime(),
                validationResult.payingPassenger(),
                validationResult.linkedPassengers()
        );

        if (validationResult.scheduledTime() == null) {
            CreatedRideResponseDTO driverError = assignDriverForImmediateRide(ride, route, requestedVehicleType);
            if (driverError != null) {
                notifyPassengerRideOrderFailed(validationResult.payingPassenger(), driverError.getMessage());
                return driverError;
            }
        } else {
            ride.setStatus(RideStatus.SCHEDULED);
        }

        ActiveRide savedRide = activeRideRepository.save(ride);
        return notifyUsers(savedRide, validationResult.scheduledTime(), validationResult.payingPassenger());
    }

    private ActiveRide buildActiveRide(
            CreateRideRequestDTO request,
            Route route,
            VehicleType requestedVehicleType,
            LocalDateTime scheduledTime,
            Passenger payingPassenger,
            List<Passenger> linkedPassengers
    ) {
        ActiveRide ride = new ActiveRide();
        ride.setRoute(route);
        ride.setVehicleType(requestedVehicleType);
        ride.setScheduledTime(scheduledTime);
        ride.setNeedsBabySeats(request.getHasBaby() != null && request.getHasBaby());
        ride.setNeedsPetFriendly(request.getHasPets() != null && request.getHasPets());
        ride.setPayingPassenger(payingPassenger);
        ride.setLinkedPassengers(linkedPassengers);
        ride.setCurrentLocation(route.getWaypoints().getFirst());
        return ride;
    }

    private CreatedRideResponseDTO assignDriverForImmediateRide(ActiveRide ride, Route route, VehicleType requestedVehicleType) {
        // Set ride vehicle type before driver is picked
        ride.setVehicleType(requestedVehicleType);

        Driver driver = driverMatchingService.findAvailableDriver(ride);

        if (driver == null) {
            return new CreatedRideResponseDTO(
                    "NO_DRIVERS_AVAILABLE",
                    "No drivers available at the moment",
                    null
            );
        }

        ride.setDriver(driver);
        ride.setVehicleType(driver.getVehicle().getType());
        ride.setEstimatedPrice(ridePriceService.calculateRidePrice(requestedVehicleType, route.getEstDistanceKm()));

        if (activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE)) {
            ride.setStatus(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE);
        } else {
            ride.setStatus(RideStatus.DRIVER_READY);
        }

        return null;
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

    private CreatedRideResponseDTO notifyUsers(ActiveRide ride, LocalDateTime scheduledTime, Passenger passenger) {
        notifyDriver(ride);

        if (scheduledTime == null) {
            notifyPassengerRideOrderedSuccessfully(passenger);
        } else {
            notifyPassengerRideScheduledSuccessfully(passenger);
        }

        return new CreatedRideResponseDTO(
                "SUCCESS",
                scheduledTime != null
                        ? "Ride scheduled successfully. Driver will be assigned closer to scheduled time."
                        : "Ride ordered successfully!",
                ride.getId()
        );
    }

    private void notifyDriver(ActiveRide savedRide) {
        if (savedRide.getStatus() == RideStatus.DRIVER_READY) {
            GetDriverActiveRideDTO rideDTO = rideMapper.toDriverActiveRideDTO(savedRide);
            webSocketController.notifyDriverRideAssigned(savedRide.getDriver().getEmail(), rideDTO);
            notifyDriverRideAssigned(savedRide.getDriver());
        } else if (savedRide.getStatus() == RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE) {
            notifyDriverRideAssignedAfterCurrent(savedRide.getDriver());
        }
    }

    private void notifyPassengerRideScheduledSuccessfully(Passenger passenger) {
        notificationService.createAndNotify(
                passenger.getId(),
                NotificationType.RIDE_SCHEDULED,
                "Ride ordered",
                "Ride scheduled successfully. Driver will be assigned closer to scheduled time.",
                LocalDateTime.now()
        );
    }

    private void notifyPassengerRideOrderedSuccessfully(Passenger passenger) {
        notificationService.createAndNotify(
                passenger.getId(),
                NotificationType.RIDE_ORDERED,
                "Ride ordered",
                "Ride ordered successfully! Driver assigned.",
                LocalDateTime.now()
        );
    }

    private void notifyPassengerRideOrderFailed(Passenger passenger, String reason) {
        notificationService.createAndNotify(
                passenger.getId(),
                NotificationType.RIDE_REJECTED,
                "Ride order failed",
                reason,
                LocalDateTime.now()
        );
    }

    private void notifyDriverRideAssigned(Driver driver) {
        notificationService.createAndNotify(
                driver.getId(),
                NotificationType.DRIVER_ASSIGNED,
                "New ride assigned",
                "A new ride has been assigned to you.",
                LocalDateTime.now()
        );
    }

    private void notifyDriverRideAssignedAfterCurrent(Driver driver) {
        notificationService.createAndNotify(
                driver.getId(),
                NotificationType.DRIVER_ASSIGNED,
                "New ride queued",
                "A new ride has been assigned to you. It will start after your current ride.",
                LocalDateTime.now()
        );
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