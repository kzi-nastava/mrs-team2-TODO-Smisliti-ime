package rs.getgo.backend.services;

import rs.getgo.backend.dtos.ride.*;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Notification;

public interface RideService {
    Notification cancelRide(ActiveRide ride, CancelRideDTO req);
    Notification cancelRideByDriver(Long rideId, String reason);
    Notification cancelRideByPassenger(Long rideId, String reason);
    UpdatedRideDTO startRide(Long rideId);
    UpdatedRideDTO acceptRide(Long rideId);
    void handleWaypointReached(ActiveRide ride);
    GetDriverActiveRideDTO getDriverActiveRide(String driverEmail);
    void triggerPanic(Long rideId, String triggeredByUserId);
    UpdatedRideDTO finishRide(Long rideId, UpdateRideDTO rideRequest);
    RideCompletionDTO stopRide(Long rideId, StopRideDTO stopRideDTO);
}
