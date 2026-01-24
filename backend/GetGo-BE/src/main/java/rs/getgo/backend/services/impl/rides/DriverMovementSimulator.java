package rs.getgo.backend.services.impl.rides;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.driver.GetDriverLocationDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.services.RideService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Moves drivers and rides based on their state
 */
@Service
public class DriverMovementSimulator {

    private final ActiveRideRepository activeRideRepository;
    private final DriverRepository driverRepository;
    private final RideService rideService;
    private final WebSocketController webSocketController;

    @Value("${simulation.speed.multiplier}")
    private int speedMultiplier;

    public DriverMovementSimulator(
            ActiveRideRepository activeRideRepository,
            DriverRepository driverRepository,
            @Lazy RideService rideService,
            WebSocketController webSocketController
    ) {
        this.activeRideRepository = activeRideRepository;
        this.driverRepository = driverRepository;
        this.rideService = rideService;
        this.webSocketController = webSocketController;
    }

    /**
     * Scheduled move drivers and rides along their paths
     */
    @Scheduled(fixedRate = 1000)
    public void updateDriverPositions() {
        List<ActiveRide> movingRides = activeRideRepository.findByStatusIn(
                List.of(RideStatus.DRIVER_INCOMING, RideStatus.ACTIVE)
        );

        for (ActiveRide ride : movingRides) {
            try {
                updateSingleDriverPosition(ride);
            } catch (Exception e) {
                System.err.println("Failed to update driver position for ride " + ride.getId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Broadcast active driver location to:
     * - Driver themselves
     * - To passengers of driver's ride if driver has active ride
     */
    @Scheduled(fixedRate = 1000)
    public void broadcastAllActiveDriverLocations() {
        List<Driver> activeDrivers = driverRepository.findByIsActive(true);

        for (Driver driver : activeDrivers) {
            try {
                // Skip drivers without location set
                if (driver.getCurrentLatitude() == null || driver.getCurrentLongitude() == null) {
                    continue;
                }

                // Find if driver has an active ride
                ActiveRide activeRide = activeRideRepository
                        .findByDriverAndStatusIn(
                                driver,
                                List.of(
                                        RideStatus.DRIVER_READY,
                                        RideStatus.DRIVER_INCOMING,
                                        RideStatus.DRIVER_ARRIVED,
                                        RideStatus.ACTIVE
                                )
                        )
                        .stream()
                        .findFirst()
                        .orElse(null);

                GetDriverLocationDTO locationUpdate = new GetDriverLocationDTO(
                        driver.getId(),
                        activeRide != null ? activeRide.getId() : null,
                        driver.getCurrentLatitude(),
                        driver.getCurrentLongitude(),
                        activeRide != null ? activeRide.getStatus().toString() : ""
                );

                webSocketController.broadcastDriverLocation(driver.getEmail(), locationUpdate);
                if (activeRide != null) {
                    webSocketController.broadcastDriverLocationToRide(activeRide.getId(), locationUpdate);
                }

            } catch (Exception e) {
                System.err.println("Failed to broadcast location for driver " + driver.getId() + ": " + e.getMessage());
            }
        }
    }

    private void updateSingleDriverPosition(ActiveRide ride) {
        if (ride.getMovementPathJson() == null || ride.getMovementPathJson().isEmpty()) return;

        List<MapboxRoutingService.Coordinate> path = parseJsonToCoordinates(ride.getMovementPathJson());
        if (path.isEmpty()) return;

        int currentIndex = ride.getCurrentPathIndex();

        // Skip indexes by multiplier
        int nextIndex = Math.min(currentIndex + speedMultiplier, path.size() - 1);

        // Check for end of waypoint reached/waypoint passed due to multiplier
        if (nextIndex >= path.size() - 1) {
            // Adjust position on waypoint
            MapboxRoutingService.Coordinate finalPosition = path.getLast();
            updateDriverLocation(ride, finalPosition, path.size() - 1);

            // Handle waypoint reached
            rideService.handleWaypointReached(ride);
            return;
        }

        // Move to next coordinate
        MapboxRoutingService.Coordinate nextPosition = path.get(currentIndex + 1);

        updateDriverLocation(ride, nextPosition, nextIndex);
    }

    private void updateDriverLocation(ActiveRide ride, MapboxRoutingService.Coordinate position, int pathIndex) {
        Driver driver = ride.getDriver();
        driver.setCurrentLatitude(position.latitude());
        driver.setCurrentLongitude(position.longitude());
        driver.setLastLocationUpdate(LocalDateTime.now());
        driverRepository.save(driver);

        // Update ride
        ride.setCurrentPathIndex(pathIndex);
        activeRideRepository.save(ride);

        // Broadcast to frontend
        GetDriverLocationDTO locationUpdate = new GetDriverLocationDTO(
                driver.getId(),
                ride.getId(),
                position.latitude(),
                position.longitude(),
                ride.getStatus().toString()
        );
        webSocketController.broadcastDriverLocation(driver.getEmail(), locationUpdate);
        webSocketController.broadcastDriverLocationToRide(ride.getId(), locationUpdate);
    }

    private List<MapboxRoutingService.Coordinate> parseJsonToCoordinates(String json) {
        List<MapboxRoutingService.Coordinate> coords = new ArrayList<>();
        try {
            String cleaned = json.replace("[", "").replace("]", "").trim();
            if (cleaned.isEmpty()) return coords;

            String[] tokens = cleaned.split(",");
            for (int i = 0; i < tokens.length - 1; i += 2) {
                double lng = Double.parseDouble(tokens[i].trim());
                double lat = Double.parseDouble(tokens[i + 1].trim());
                coords.add(new MapboxRoutingService.Coordinate(lat, lng));
            }
        } catch (Exception e) {
            System.err.println("Failed to parse movement path: " + e.getMessage());
        }
        return coords;
    }
}