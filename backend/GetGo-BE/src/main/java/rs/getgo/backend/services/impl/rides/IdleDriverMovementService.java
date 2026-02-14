package rs.getgo.backend.services.impl.rides;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.driver.GetDriverLocationDTO;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.DriverRoamingState;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.repositories.DriverRoamingStateRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class IdleDriverMovementService {

    private final DriverRepository driverRepository;
    private final ActiveRideRepository activeRideRepository;
    private final DriverRoamingStateRepository roamingStateRepository;
    private final MapboxRoutingService routingService;
    private final WebSocketController webSocketController;

    // Key: fromIndex-toIndex, value: coordinate list
    private final Map<String, List<MapboxRoutingService.Coordinate>> pathCache = new ConcurrentHashMap<>();

    // Fixed coordinates drivers can move to
    private static final List<MapboxRoutingService.Coordinate> WAYPOINTS = List.of(
            new MapboxRoutingService.Coordinate(45.253508, 19.789307),
            new MapboxRoutingService.Coordinate(45.242382, 19.796210),
            new MapboxRoutingService.Coordinate(45.239636, 19.823478),
            new MapboxRoutingService.Coordinate(45.241173, 19.850587),
            new MapboxRoutingService.Coordinate(45.255172, 19.835326),
            new MapboxRoutingService.Coordinate(45.270722, 19.841983),
            new MapboxRoutingService.Coordinate(45.264318, 19.816974),
            new MapboxRoutingService.Coordinate(45.255894, 19.814524),
            new MapboxRoutingService.Coordinate(45.260139, 19.838934)
    );

    public IdleDriverMovementService(
            DriverRepository driverRepository,
            ActiveRideRepository activeRideRepository,
            DriverRoamingStateRepository roamingStateRepository,
            MapboxRoutingService routingService,
            WebSocketController webSocketController
    ) {
        this.driverRepository = driverRepository;
        this.activeRideRepository = activeRideRepository;
        this.roamingStateRepository = roamingStateRepository;
        this.routingService = routingService;
        this.webSocketController = webSocketController;
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void updateIdleDriverPositions() {
        List<Driver> activeDrivers = driverRepository.findByIsActive(true);

        for (Driver driver : activeDrivers) {
            try {
                if (activeRideRepository.existsByDriverAndStatusNot(driver, RideStatus.SCHEDULED)) {
                    roamingStateRepository.deleteByDriver(driver);
                    continue;
                }
                moveIdleDriver(driver);
            } catch (Exception e) {
                System.err.println("Error for driver " + driver.getId() + ": " + e.getMessage());
            }
        }
    }

    private void moveIdleDriver(Driver driver) {
        DriverRoamingState state = roamingStateRepository.findByDriver(driver).orElse(null);

        if (state == null || state.getMovementPathJson() == null || state.getMovementPathJson().isEmpty()) {
            assignNewRoamingPath(driver, state);
            return;
        }

        List<MapboxRoutingService.Coordinate> path = routingService.parseJsonToCoordinates(state.getMovementPathJson());
        if (path.isEmpty()) {
            assignNewRoamingPath(driver, state);
            return;
        }

        int currentIndex = state.getCurrentPathIndex();
        int nextIndex = currentIndex + 1;

        if (nextIndex >= path.size()) {
            assignNewRoamingPath(driver, state);
            return;
        }

        MapboxRoutingService.Coordinate nextPos = path.get(nextIndex);

        driver.setCurrentLatitude(nextPos.latitude());
        driver.setCurrentLongitude(nextPos.longitude());
        driver.setLastLocationUpdate(LocalDateTime.now());
        driverRepository.save(driver);

        state.setCurrentPathIndex(nextIndex);
        roamingStateRepository.save(state);

        GetDriverLocationDTO locationUpdate = new GetDriverLocationDTO(
                driver.getId(),
                null, // idle driver doesn't have an active ride
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude(),
                "" // IDLE
        );

        webSocketController.broadcastAllDriversLocation(locationUpdate);
    }

    private void assignNewRoamingPath(Driver driver, DriverRoamingState existingState) {
        int fromWaypoint = pickRandomWaypointExcluding(-1);
        int toWaypoint = pickRandomWaypointExcluding(fromWaypoint);

        MapboxRoutingService.Coordinate startPos = WAYPOINTS.get(fromWaypoint);
        driver.setCurrentLatitude(startPos.latitude());
        driver.setCurrentLongitude(startPos.longitude());
        driver.setLastLocationUpdate(LocalDateTime.now());
        driverRepository.save(driver);

        List<MapboxRoutingService.Coordinate> path = getOrFetchPath(fromWaypoint, toWaypoint);
        if (path == null || path.isEmpty()) return;

        String pathJson = routingService.convertCoordinatesToJson(path);

        DriverRoamingState state = existingState;
        if (state == null) {
            state = new DriverRoamingState();
            state.setDriver(driver);
        }

        state.setMovementPathJson(pathJson);
        state.setCurrentPathIndex(0);
        state.setTargetWaypointIndex(toWaypoint);
        roamingStateRepository.save(state);

        webSocketController.broadcastAllDriversLocation(new GetDriverLocationDTO(
                driver.getId(),
                null,
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude(),
                "" // IDLE
        ));
    }

    private List<MapboxRoutingService.Coordinate> getOrFetchPath(int fromIndex, int toIndex) {
        String key = fromIndex + "-" + toIndex;

        return pathCache.computeIfAbsent(key, k -> {
            MapboxRoutingService.Coordinate from = WAYPOINTS.get(fromIndex);
            MapboxRoutingService.Coordinate to = WAYPOINTS.get(toIndex);
            try {
                MapboxRoutingService.RouteResponse route = routingService.getRoute(
                        from.latitude(), from.longitude(),
                        to.latitude(), to.longitude()
                );
                System.out.println("[Roaming] Fetched and cached path " + key);
                return route.coordinates();
            } catch (Exception e) {
                System.err.println("[Roaming] Failed to fetch path " + key + ": " + e.getMessage());
                return List.of();
            }
        });
    }
    private int pickRandomWaypointExcluding(int excludeIndex) {
        int idx;
        do {
            idx = ThreadLocalRandom.current().nextInt(WAYPOINTS.size());
        } while (idx == excludeIndex);
        return idx;
    }
}