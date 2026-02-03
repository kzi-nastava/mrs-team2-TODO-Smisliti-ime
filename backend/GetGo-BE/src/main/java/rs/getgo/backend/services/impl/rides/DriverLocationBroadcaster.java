package rs.getgo.backend.services.impl.rides;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.driver.GetDriverLocationDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.DriverRepository;

import java.util.List;

@Service
public class DriverLocationBroadcaster {

    private final DriverRepository driverRepository;
    private final ActiveRideRepository activeRideRepository;
    private final WebSocketController webSocketController;

    public DriverLocationBroadcaster(
            DriverRepository driverRepository,
            ActiveRideRepository activeRideRepository,
            WebSocketController webSocketController
    ) {
        this.driverRepository = driverRepository;
        this.activeRideRepository = activeRideRepository;
        this.webSocketController = webSocketController;
    }

    /**
     * Broadcast all active driver locations every x seconds
     * - To drivers themselves (for their own tracking)
     * - To passengers (for tracking their assigned driver)
     */
    @Scheduled(fixedRate = 1000)
    public void broadcastAllActiveDriverLocations() {
        List<Driver> activeDrivers = driverRepository.findByIsActive(true);

        for (Driver driver : activeDrivers) {
            try {
                broadcastDriverLocation(driver);
            } catch (Exception e) {
                System.err.println("Failed to broadcast location for driver " + driver.getId() + ": " + e.getMessage());
            }
        }
    }

    private void broadcastDriverLocation(Driver driver) {
        ActiveRide activeRide = activeRideRepository
                .findByDriverAndStatusIn(
                        driver,
                        List.of(
                                RideStatus.DRIVER_READY,
                                RideStatus.DRIVER_INCOMING,
                                RideStatus.DRIVER_ARRIVED,
                                RideStatus.ACTIVE,
                                RideStatus.DRIVER_ARRIVED_AT_DESTINATION
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
    }

    // TODO: potentially add another broadcast-all-active-drivers function that's set off less regularly (5-10s?)
    // TODO: this function will allow unregistered and registered passengers to see locations of all active drivers
}
