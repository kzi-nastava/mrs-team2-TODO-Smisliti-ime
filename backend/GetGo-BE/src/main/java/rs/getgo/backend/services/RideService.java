package rs.getgo.backend.services;

import rs.getgo.backend.dtos.ride.*;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.model.entities.Notification;

public interface RideService {
    // cancel a ride (passenger/driver) and create a notification for participants; returns the Notification created
    public Notification cancelRide(ActiveRide ride, CancelRideDTO req);
    Notification cancelRideByDriver(Long rideId, String reason);
    Notification cancelRideByPassenger(Long rideId, String reason);
    public CreatedRideResponseDTO orderRide(CreateRideRequestDTO createRideRequestDTO, String email);
    UpdatedRideDTO startRide(Long rideId);
    UpdatedRideDTO acceptRide(Long rideId);
    void handleWaypointReached(ActiveRide ride);
    GetDriverActiveRideDTO getDriverActiveRide(String driverEmail);
    void triggerPanic(Long rideId, String triggeredByUserId);
    public UpdatedRideDTO finishRide(Long rideId, UpdateRideDTO rideRequest);
    public RideCompletionDTO stopRide(Long rideId, StopRideDTO stopRideDTO);
}
