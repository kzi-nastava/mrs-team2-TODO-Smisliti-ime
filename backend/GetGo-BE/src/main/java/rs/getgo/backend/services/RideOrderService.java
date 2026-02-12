package rs.getgo.backend.services;

import rs.getgo.backend.dtos.ride.CreateRideRequestDTO;
import rs.getgo.backend.dtos.ride.CreatedRideResponseDTO;

public interface RideOrderService {
    CreatedRideResponseDTO orderRide(CreateRideRequestDTO createRideRequestDTO, String userEmail);
}
