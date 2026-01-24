package rs.getgo.backend.services.impl;

import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.services.CompletedRideService;

@Service
public class CompletedRideServiceImpl implements CompletedRideService {
    private final CompletedRideRepository completedRideRepository;

    public CompletedRideServiceImpl(CompletedRideRepository completedRideRepository) {
        this.completedRideRepository = completedRideRepository;
    }

    public Long getDriverIdByRideId(Long rideId) {
        CompletedRide ride = completedRideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        return ride.getDriverId();
    }
}
