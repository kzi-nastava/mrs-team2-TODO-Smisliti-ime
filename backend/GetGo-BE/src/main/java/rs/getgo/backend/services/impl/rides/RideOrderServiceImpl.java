package rs.getgo.backend.services.impl.rides;

import org.springframework.stereotype.Service;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.ride.CreateRideRequestDTO;
import rs.getgo.backend.dtos.ride.CreatedRideResponseDTO;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.dtos.ridePrice.GetRidePriceDTO;
import rs.getgo.backend.mappers.RideMapper;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RideOrderStatus;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.*;
import rs.getgo.backend.services.DriverMatchingService;
import rs.getgo.backend.services.RideOrderService;

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
    private final RidePriceRepository ridePriceRepository;
    private final MapboxRoutingService routingService;
    private final DriverMatchingService driverMatchingService;
    private final RideMapper rideMapper;

    public RideOrderServiceImpl(
            WebSocketController webSocketController,
            PassengerRepository passengerRepository,
            BlockNoteRepository blockNoteRepository,
            RouteRepository routeRepository,
            ActiveRideRepository activeRideRepository,
            RidePriceRepository ridePriceRepository,
            MapboxRoutingService routingService,
            DriverMatchingService driverMatchingService,
            RideMapper rideMapper
    ) {
        this.webSocketController = webSocketController;
        this.passengerRepository = passengerRepository;
        this.blockNoteRepository = blockNoteRepository;
        this.routeRepository = routeRepository;
        this.activeRideRepository = activeRideRepository;
        this.ridePriceRepository = ridePriceRepository;
        this.routingService = routingService;
        this.driverMatchingService = driverMatchingService;
        this.rideMapper = rideMapper;
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
        if (payingPassenger.isBlocked()) {
            String reason = blockNoteRepository.findByUserAndUnblockedAtIsNull(payingPassenger)
                    .map(BlockNote::getReason)
                    .orElse("You have been blocked.");
            return new CreatedRideResponseDTO(
                    "blocked",
                    "Cannot order ride: user is blocked. Reason: " + reason,
                    null);
        }

        // Check if paying passenger already has an active ride
        boolean hasActiveRide = activeRideRepository.existsByPayingPassengerOrLinkedPassengersContaining(
                payingPassenger, payingPassenger
        );
        if (hasActiveRide) {
            return new CreatedRideResponseDTO(
                    "PASSENGER_HAS_ACTIVE_RIDE",
                    "You already have an active ride",
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

        // Find and validate linked passengers
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

                // Check if linked passenger already has an active ride
                boolean linkedHasActiveRide = activeRideRepository.existsByPayingPassengerOrLinkedPassengersContaining(
                        passenger, passenger
                );
                if (linkedHasActiveRide) {
                    return new CreatedRideResponseDTO(
                            "LINKED_PASSENGER_HAS_ACTIVE_RIDE",
                            "Passenger " + email + " already has an active ride",
                            null
                    );
                }

                linkedPassengers.add(passenger);
            }
        }

        // Create Route with waypoints
        Route route = createRoute(createRideRequestDTO);
        routeRepository.save(route);

        // Parse vehicle type (null for "ANY")
        VehicleType requestedVehicleType = parseVehicleType(createRideRequestDTO.getVehicleType());

        // Create ActiveRide with common properties
        ActiveRide ride = new ActiveRide();
        ride.setRoute(route);
        ride.setScheduledTime(scheduledTime);
        ride.setNeedsBabySeats(createRideRequestDTO.getHasBaby() != null && createRideRequestDTO.getHasBaby());
        ride.setNeedsPetFriendly(createRideRequestDTO.getHasPets() != null && createRideRequestDTO.getHasPets());
        ride.setPayingPassenger(payingPassenger);
        ride.setLinkedPassengers(linkedPassengers);
        ride.setCurrentLocation(route.getWaypoints().getFirst());

        if (scheduledTime == null) {
            // Assign driver for immediate rides
            Driver driver = driverMatchingService.findAvailableDriver(ride);

            if (driver == null) {
                return new CreatedRideResponseDTO(
                        "NO_DRIVERS_AVAILABLE",
                        "No drivers available at the moment",
                        null
                );
            }

            ride.setDriver(driver);

            // Use driver's vehicle type (overrides "ANY" if requested)
            VehicleType actualVehicleType = driver.getVehicle().getType();
            ride.setVehicleType(actualVehicleType);
            ride.setEstimatedPrice(calculatePrice(route, actualVehicleType.toString()));

            // Set status based on driver's current state
            if (activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE)) {
                ride.setStatus(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE);
            } else {
                ride.setStatus(RideStatus.DRIVER_READY);
            }
        } else {
            // Assign driver later for scheduled rides
            ride.setVehicleType(requestedVehicleType);
            // TODO: on scheduled ride activate when driver is picked calculate estimated price
            ride.setStatus(RideStatus.SCHEDULED);
        }

        // Save ride
        ActiveRide savedRide = activeRideRepository.save(ride);

        // Notify driver if ready
        if (savedRide.getStatus() == RideStatus.DRIVER_READY) {
            GetDriverActiveRideDTO rideDTO = rideMapper.buildDriverActiveRideDTO(savedRide);
            webSocketController.notifyDriverRideAssigned(savedRide.getDriver().getEmail(), rideDTO);
        }

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

            // Collect all coordinates for polyline
            if (i == 0) {
                allCoordinates.addAll(segment.coordinates());
            } else {
                // Skip first coordinate to avoid duplicates at waypoint connections
                allCoordinates.addAll(segment.coordinates().subList(1, segment.coordinates().size()));
            }
        }

        route.setEstDistanceKm(totalDistance);
        route.setEstTimeMin(totalTime);

        // Save the polyline as JSON string
        String polylineJson = routingService.convertCoordinatesToJson(allCoordinates);
        route.setEncodedPolyline(polylineJson);

        return route;
    }

    private double calculatePrice(Route route, String vehicleTypeStr) {
        VehicleType vehicleType = parseVehicleType(vehicleTypeStr);
        GetRidePriceDTO priceDTO = getPricesWithDefaults(vehicleType);
        return priceDTO.getStartPrice() + (route.getEstDistanceKm() * priceDTO.getPricePerKm());
    }

    private GetRidePriceDTO getPricesWithDefaults(VehicleType vehicleType) {
        // Default values if not set in database
        double defaultStartPrice;
        double defaultPricePerKm;

        switch (vehicleType) {
            case STANDARD -> { defaultStartPrice = 200; defaultPricePerKm = 120; }
            case VAN -> { defaultStartPrice = 500; defaultPricePerKm = 150; }
            case LUXURY -> { defaultStartPrice = 800; defaultPricePerKm = 200; }
            default -> { defaultStartPrice = 200; defaultPricePerKm = 100; }
        }

        return ridePriceRepository.findByVehicleType(vehicleType)
                .map(p -> new GetRidePriceDTO(p.getPricePerKm(), p.getStartPrice()))
                .orElse(new GetRidePriceDTO(defaultPricePerKm, defaultStartPrice));
    }


    private VehicleType parseVehicleType(String vehicleTypeStr) {
        if (vehicleTypeStr == null || vehicleTypeStr.trim().isEmpty()) {
            return null; // Vehicle type is 'any'
        }

        try {
            return VehicleType.valueOf(vehicleTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
