package rs.getgo.backend.services;

import rs.getgo.backend.dtos.ride.CancelRideDTO;
import rs.getgo.backend.dtos.rideStatus.CreatedRideStatusDTO;

public interface RideService {
    // cancel a ride (passenger/driver) and return resulting status
    public CreatedRideStatusDTO cancelRide(Long rideId, CancelRideDTO req);
<<<<<<< Updated upstream

    // estimate and stop are left as void hooks (implementation may extend)
    public void stopRide();

    void triggerPanic(Long rideId, String triggeredByUserId);
=======
    public CreatedRideResponseDTO orderRide(CreateRideRequestDTO createRideRequestDTO, String email);
    UpdatedRideDTO startRide(Long rideId);
    UpdatedRideDTO acceptRide(Long rideId);
    void handleWaypointReached(ActiveRide ride);
    GetDriverActiveRideDTO getDriverActiveRide(String driverEmail);
    void triggerPanic(Long rideId, String triggeredByUserId);
    public UpdatedRideDTO finishRide(Long rideId, UpdateRideDTO rideRequest);
    public RideCompletionDTO stopRide(Long rideId, StopRideDTO stopRideDTO);
>>>>>>> Stashed changes
}
