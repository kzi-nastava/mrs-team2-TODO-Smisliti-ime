package rs.getgo.backend.services.impl.rides;

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

        // Check for end of waypoint reached
        if (currentIndex >= path.size() - 1) {
            rideService.handleWaypointReached(ride);
            return;
        }

        // Move to next coordinate
        MapboxRoutingService.Coordinate nextPosition = path.get(currentIndex + 1);

        // Update driver location
        Driver driver = ride.getDriver();
        driver.setCurrentLatitude(nextPosition.latitude());
        driver.setCurrentLongitude(nextPosition.longitude());
        driver.setLastLocationUpdate(LocalDateTime.now());
        driverRepository.save(driver);

        // Update ride
        ride.setCurrentPathIndex(currentIndex + 1);
        activeRideRepository.save(ride);

        // Update front
        GetDriverLocationDTO locationUpdate = new GetDriverLocationDTO(
                driver.getId(),
                ride.getId(),
                nextPosition.latitude(),
                nextPosition.longitude(),
                ride.getStatus().toString()
        );
        // Send to driver
        webSocketController.broadcastDriverLocation(driver.getId(), locationUpdate);
        // Send to passengers
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