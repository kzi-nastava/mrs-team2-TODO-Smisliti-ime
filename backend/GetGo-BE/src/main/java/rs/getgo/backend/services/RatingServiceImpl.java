package rs.getgo.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.rating.CreateRatingDTO;
import rs.getgo.backend.dtos.rating.CreatedRatingDTO;
import rs.getgo.backend.dtos.rating.GetRatingDTO;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.entities.Rating;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.repositories.RatingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final DriverRepository driverRepository;

    public RatingServiceImpl(
            RatingRepository ratingRepository,
            DriverRepository driverRepository
    ) {
        this.ratingRepository = ratingRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public List<GetRatingDTO> getRatingsByRide(Long rideId) {
        List<Rating> ratings = ratingRepository.findByCompletedRide_Id(rideId);
        List<GetRatingDTO> result = new ArrayList<>();

        for (Rating rating : ratings) {
            result.add(mapToGetDTO(rating));
        }

        return result;
    }

    private GetRatingDTO mapToGetDTO(Rating r) {
        CompletedRide ride = r.getCompletedRide();

        Driver driver = driverRepository.findById(ride.getDriverId()).orElse(null);
        Long vehicleId = null;
        if (driver != null && driver.getVehicle() != null) {
            vehicleId = driver.getVehicle().getId();
        }

        return new GetRatingDTO(
                r.getId(),
                ride.getId(),
                ride.getDriverId(),
                vehicleId,
                r.getPassenger().getId(),
                r.getDriverRating(),
                r.getVehicleRating(),
                r.getComment()
        );
    }

    @Override
    public CreatedRatingDTO create(CreateRatingDTO dto, CompletedRide ride, Long passengerId) {
        Rating rating = new Rating();
        rating.setCompletedRide(ride);

        Passenger passenger = new Passenger();
//        passenger.setId(1L); // temporary hardcoded passenger ID
        passenger.setId(passengerId);
        rating.setPassenger(passenger);

        rating.setDriverRating(dto.getDriverRating());
        rating.setVehicleRating(dto.getVehicleRating());
        rating.setComment(dto.getComment());

        Rating saved = ratingRepository.save(rating);

        Driver driver = driverRepository.findById(ride.getDriverId()).orElse(null);
        Long vehicleId = null;
        if (driver != null && driver.getVehicle() != null) {
            vehicleId = driver.getVehicle().getId();
        }

        return new CreatedRatingDTO(
                saved.getId(),
                passengerId,
                ride.getId(),
                ride.getDriverId(),
                vehicleId,
                saved.getDriverRating(),
                saved.getVehicleRating(),
                saved.getComment()
        );
    }

    @Override
    public boolean hasUserRatedRide(Long passengerId, Long rideId) {
        return ratingRepository.existsByPassenger_IdAndCompletedRide_Id(passengerId, rideId);
    }



}
