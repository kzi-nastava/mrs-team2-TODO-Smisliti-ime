package rs.getgo.backend.services;

import rs.getgo.backend.dtos.ride.*;
import rs.getgo.backend.model.entities.ActiveRide;

public interface RideService {
    // cancel a ride (passenger/driver) and return resulting status
    public void cancelRide(ActiveRide ride, CancelRideDTO req);
    void cancelRideByDriver(Long rideId, String reason);
    void cancelRideByPassenger(Long rideId, String reason);
    public CreatedRideResponseDTO orderRide(CreateRideRequestDTO createRideRequestDTO, String email);
    UpdatedRideDTO startRide(Long rideId);
    UpdatedRideDTO acceptRide(Long rideId);
    void handleWaypointReached(ActiveRide ride);
    GetDriverActiveRideDTO getDriverActiveRide(String driverEmail);
    void triggerPanic(Long rideId, String triggeredByUserId);
    public UpdatedRideDTO finishRide(Long rideId, UpdateRideDTO rideRequest);
    public RideCompletionDTO stopRide(Long rideId, StopRideDTO stopRideDTO);
}

