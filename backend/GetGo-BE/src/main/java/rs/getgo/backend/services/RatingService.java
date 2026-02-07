package rs.getgo.backend.services;

import org.springframework.security.core.Authentication;
import rs.getgo.backend.dtos.rating.CreateRatingDTO;
import rs.getgo.backend.dtos.rating.CreatedRatingDTO;
import rs.getgo.backend.dtos.rating.GetRatingDTO;
import rs.getgo.backend.model.entities.CompletedRide;

import java.util.List;

public interface RatingService {
    List<GetRatingDTO> getRatingsByRide(Long rideId);
    boolean hasUserRatedRide(Long passengerId, Long rideId);
    public List<GetRatingDTO> getRatingsByDriver(Long driverId);
    CreatedRatingDTO createRating(CreateRatingDTO dto, Long rideId, Authentication auth);

}
