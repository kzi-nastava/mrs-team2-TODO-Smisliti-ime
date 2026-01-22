package rs.getgo.backend.services;

import rs.getgo.backend.dtos.ride.*;
import rs.getgo.backend.dtos.rideStatus.CreatedRideStatusDTO;
import rs.getgo.backend.model.entities.ActiveRide;

public interface RideService {
    // cancel a ride (passenger/driver) and return resulting status
    public CreatedRideStatusDTO cancelRide(Long rideId, CancelRideDTO req);

    // estimate and stop are left as void hooks (implementation may extend)
    public void stopRide();

    public CreatedRideResponseDTO orderRide(CreateRideRequestDTO createRideRequestDTO, String email);
    UpdatedRideDTO startRide(Long rideId);
    void handleWaypointReached(ActiveRide ride);

    GetDriverActiveRideDTO getDriverActiveRide(String driverEmail);


    void triggerPanic(Long rideId, String triggeredByUserId);

}
