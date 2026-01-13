package rs.getgo.backend.services;

import org.springframework.web.bind.annotation.RequestBody;
import rs.getgo.backend.dtos.rideEstimate.CreateRideEstimateDTO;
import rs.getgo.backend.dtos.rideEstimate.CreatedRideEstimateDTO;

import java.util.Optional;

public interface RideEstimateService {
    public CreatedRideEstimateDTO createEstimate(@RequestBody CreateRideEstimateDTO request);
}
