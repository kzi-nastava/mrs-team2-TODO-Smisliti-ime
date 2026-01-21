package rs.getgo.backend.services;

import rs.getgo.backend.dtos.ride.*;
import rs.getgo.backend.dtos.rideStatus.CreatedRideStatusDTO;

public interface RideService {
    // cancel a ride (passenger/driver) and return resulting status
    public CreatedRideStatusDTO cancelRide(Long rideId, CancelRideDTO req);

    // estimate and stop are left as void hooks (implementation may extend)
    public void stopRide();

    public CreatedRideResponseDTO orderRide(CreateRideRequestDTO createRideRequestDTO, String email);
    UpdatedRideDTO startRide(Long rideId);
    GetDriverActiveRideDTO getDriverActiveRide(String driverEmail);

    void triggerPanic(Long rideId, String triggeredByUserId);

}
