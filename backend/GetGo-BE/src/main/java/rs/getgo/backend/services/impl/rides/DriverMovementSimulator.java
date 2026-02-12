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

    private void updateSingleDriverPosition(ActiveRide ride) {
        if (ride.getMovementPathJson() == null || ride.getMovementPathJson().isEmpty()) return;

        List<MapboxRoutingService.Coordinate> path = parseJsonToCoordinates(ride.getMovementPathJson());
        if (path.isEmpty()) return;

        int currentIndex = ride.getCurrentPathIndex();
        int nextIndex = Math.min(currentIndex + speedMultiplier, path.size() - 1);

        // Move to next coordinate
        MapboxRoutingService.Coordinate nextPosition = path.get(nextIndex);
        updateDriverLocation(ride, nextPosition, nextIndex);

        // Check if waypoint reached
        if (nextIndex >= path.size() - 1) {
            rideService.handleWaypointReached(ride);
        }

        // Always broadcast after update
        broadcastDriverLocation(ride.getDriver(), ride);
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
    }

    private void broadcastDriverLocation(Driver driver, ActiveRide ride) {
        GetDriverLocationDTO locationUpdate = new GetDriverLocationDTO(
                driver.getId(),
                ride.getId(),
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude(),
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