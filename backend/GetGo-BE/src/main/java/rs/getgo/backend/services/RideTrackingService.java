package rs.getgo.backend.services;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.ride.GetRideTrackingDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Route;
import rs.getgo.backend.model.entities.WayPoint;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.ActiveRideRepository;

@Service
public class RideTrackingService {

    private final ActiveRideRepository activeRideRepository;

    public RideTrackingService(ActiveRideRepository activeRideRepository) {
        this.activeRideRepository = activeRideRepository;
    }

    public GetRideTrackingDTO getRideTracking(Long rideId) {
        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        WayPoint currentLocation = ride.getCurrentLocation();
        Route route = ride.getRoute();

        // Calculate estimated time remaining in minutes
        double estimatedTimeMinutes = calculateEstimatedTime(currentLocation, route, ride.getVehicleType());

        return new GetRideTrackingDTO(
                ride.getId(),
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                route.getStartingPoint(),
                route.getEndingPoint(),
                estimatedTimeMinutes
        );
    }

    private double calculateEstimatedTime(WayPoint current, Route route, VehicleType vehicleType) {
        // For now, simplified estimate using route's estimated time
        // In the future, replace with API call to Mapbox/OSRM
        return route.getEstTimeMin();
    }


}
