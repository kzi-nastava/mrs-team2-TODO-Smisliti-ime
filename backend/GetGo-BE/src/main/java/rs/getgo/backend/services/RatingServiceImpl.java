package rs.getgo.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.rating.CreateRatingDTO;
import rs.getgo.backend.dtos.rating.CreatedRatingDTO;
import rs.getgo.backend.dtos.rating.GetRatingDTO;
import rs.getgo.backend.exceptions.RatingException;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.entities.Rating;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.repositories.RatingRepository;

import java.time.LocalDateTime;
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
        // Check if the ride has actually finished
        if (ride.getEndTime() == null) {
            throw new RatingException("RIDE_NOT_FINISHED", "Cannot rate a ride that has not finished");
        }

        // Check if the current time is within 3 days from ride completion
        LocalDateTime now = LocalDateTime.now();
        if (ride.getEndTime().plusDays(3).isBefore(LocalDateTime.now())) {
            throw new RatingException("EXPIRED", "You can rate this ride only within 3 days of completion");
        }

        // Check if the passenger has already rated this ride
        if (hasUserRatedRide(passengerId, ride.getId())) {
            throw new RatingException("ALREADY_RATED", "You have already rated this ride");
        }

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

    @Override
    public List<GetRatingDTO> getRatingsByDriver(Long driverId) {
        List<Rating> ratings = ratingRepository
                .findByCompletedRide_DriverId(driverId);

        return ratings.stream()
                .map(this::mapToGetDTO)
                .toList();
    }



}
