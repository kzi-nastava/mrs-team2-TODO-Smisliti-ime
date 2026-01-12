package rs.getgo.backend.services;

import rs.getgo.backend.dtos.rideEstimate.CreateRideEstimateDTO;
import rs.getgo.backend.dtos.rideEstimate.CreatedRideEstimateDTO;

import java.util.Optional;

public interface RideEstimateService {
    public CreatedRideEstimateDTO createEstimate(CreateRideEstimateDTO request);
    public Optional<double[]> geocode(String fullAddress);
}
