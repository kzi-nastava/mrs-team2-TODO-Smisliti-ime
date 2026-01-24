package rs.getgo.backend.services;

import rs.getgo.backend.dtos.rating.CreateRatingDTO;
import rs.getgo.backend.dtos.rating.CreatedRatingDTO;
import rs.getgo.backend.dtos.rating.GetRatingDTO;
import rs.getgo.backend.model.entities.CompletedRide;

import java.util.List;

public interface RatingService {
    List<GetRatingDTO> getRatingsByRide(Long rideId);
    CreatedRatingDTO create(CreateRatingDTO dto, CompletedRide ride, Long passengerId);
    boolean hasUserRatedRide(Long passengerId, Long rideId);
    public List<GetRatingDTO> getRatingsByDriver(Long driverId);
}
