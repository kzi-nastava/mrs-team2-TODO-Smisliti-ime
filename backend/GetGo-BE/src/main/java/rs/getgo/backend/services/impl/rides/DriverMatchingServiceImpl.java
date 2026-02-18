package rs.getgo.backend.services.impl.rides;

import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Vehicle;
import rs.getgo.backend.model.entities.WayPoint;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.services.DriverMatchingService;
import rs.getgo.backend.services.DriverService;

import java.util.List;
import java.util.Optional;

@Service
public class DriverMatchingServiceImpl implements DriverMatchingService {

    private final DriverRepository driverRepository;
    private final ActiveRideRepository activeRideRepository;
    private final DriverService driverService;
    private final MapboxRoutingService routingService;

    public DriverMatchingServiceImpl(
            DriverRepository driverRepository,
            ActiveRideRepository activeRideRepository,
            DriverService driverService,
            MapboxRoutingService routingService
    ) {
        this.driverRepository = driverRepository;
        this.activeRideRepository = activeRideRepository;
        this.driverService = driverService;
        this.routingService = routingService;
    }

    @Override
    public Driver findAvailableDriver(ActiveRide ride) {
        WayPoint startPoint = ride.getRoute().getWaypoints().getFirst();
        double lat = startPoint.getLatitude();
        double lng = startPoint.getLongitude();

        List<Driver> candidates = driverRepository.findByIsActive(true)
                .stream()
                .filter(d -> !d.isBlocked())
                .filter(d -> vehicleMeetsRideRequirements(d, ride))
                .filter(d -> !driverService.hasExceededWorkingHours(d))
                .toList();
        if (candidates.isEmpty()) return null;

        // Try to assign the closest free driver
        List<Driver> freeDrivers = candidates.stream()
                .filter(this::isFree)
                .toList();
        if (!freeDrivers.isEmpty()) return findClosestDriver(freeDrivers, lat, lng);

        // Try to assign the closest driver that's finishing his ride soon and is not reserved
        List<Driver> finishingDrivers = candidates.stream()
                .filter(this::canAcceptNextRideWhileFinishing)
                .toList();
        if (finishingDrivers.isEmpty()) return null;

        return findClosestDriver(finishingDrivers, lat, lng);
    }

    private boolean vehicleMeetsRideRequirements(Driver driver, ActiveRide ride) {
        Vehicle vehicle = driver.getVehicle();

        if (ride.getVehicleType() != null && vehicle.getType() != ride.getVehicleType()) {
            return false;
        }
        if (ride.isNeedsBabySeats() && !vehicle.getIsBabyFriendly()) {
            return false;
        }
        return !ride.isNeedsPetFriendly() || vehicle.getIsPetFriendly();
    }

    private boolean isFree(Driver driver) {
        return !activeRideRepository
                .existsByDriverAndStatusIn(
                        driver,
                        List.of(
                                RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE,
                                RideStatus.DRIVER_READY,
                                RideStatus.DRIVER_INCOMING,
                                RideStatus.DRIVER_ARRIVED,
                                RideStatus.ACTIVE,
                                RideStatus.DRIVER_ARRIVED_AT_DESTINATION
                        )
                );
    }

    private boolean canAcceptNextRideWhileFinishing(Driver driver) {

        boolean hasActive = activeRideRepository
                .existsByDriverAndStatus(driver, RideStatus.ACTIVE);

        boolean hasFutureAssigned = activeRideRepository
                .existsByDriverAndStatusIn(
                        driver,
                        List.of(
                                RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE,
                                RideStatus.DRIVER_INCOMING,
                                RideStatus.SCHEDULED
                        )
                );

        return hasActive && !hasFutureAssigned && isFinishingSoon(driver);
    }

    private boolean isFinishingSoon(Driver driver) {
        ActiveRide currentRide = activeRideRepository
                .findByDriverAndStatus(driver, RideStatus.ACTIVE)
                .orElse(null);

        if (currentRide == null || driver.getCurrentLatitude() == null || driver.getCurrentLongitude() == null) {
            return false;
        }

        // Get remaining waypoints (those not yet reached)
        List<WayPoint> remainingWaypoints = currentRide.getRoute().getWaypoints().stream()
                .filter(wp -> wp.getReachedAt() == null)
                .toList();

        if (remainingWaypoints.isEmpty()) {
            return true; // Already at destination
        }

        // Calculate remaining time from driver's current location
        double remainingMinutes = routingService.calculateRemainingTime(
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude(),
                remainingWaypoints
        );

        return remainingMinutes <= 10;
    }

    // Returns closest driver closest to target position
    // 1) Driver free: calculate distance using current driver location
    // 2) Driver finishing previous ride: calculate distance using position of the end of his current ride
    private Driver findClosestDriver(List<Driver> drivers, double targetLat, double targetLng) {
        Driver closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver d : drivers) {
            double srcLat;
            double srcLng;

            Optional<ActiveRide> active =
                    activeRideRepository.findByDriverAndStatus(d, RideStatus.ACTIVE);

            if (active.isPresent()) {
                WayPoint end = active.get()
                        .getRoute()
                        .getWaypoints()
                        .getLast();
                srcLat = end.getLatitude();
                srcLng = end.getLongitude();
            } else {
                if (d.getCurrentLatitude() == null || d.getCurrentLongitude() == null)
                    continue;
                srcLat = d.getCurrentLatitude();
                srcLng = d.getCurrentLongitude();
            }

            double dLat = srcLat - targetLat;
            double dLng = (srcLng - targetLng) * Math.cos(Math.toRadians(targetLat));
            double dist = dLat * dLat + dLng * dLng;

            if (dist < minDistance) {
                minDistance = dist;
                closest = d;
            }
        }

        return closest;
    }
}
