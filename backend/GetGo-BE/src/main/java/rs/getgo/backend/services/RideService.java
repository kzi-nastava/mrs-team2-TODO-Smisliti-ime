package rs.getgo.backend.services;

import rs.getgo.backend.dtos.ride.CancelRideDTO;
import rs.getgo.backend.dtos.rideStatus.CreatedRideStatusDTO;

public interface RideService {
    // cancel a ride (passenger/driver) and return resulting status
    public CreatedRideStatusDTO cancelRide(Long rideId, CancelRideDTO req);

    // estimate and stop are left as void hooks (implementation may extend)
    public void estimateRide();
    public void stopRide();
}
