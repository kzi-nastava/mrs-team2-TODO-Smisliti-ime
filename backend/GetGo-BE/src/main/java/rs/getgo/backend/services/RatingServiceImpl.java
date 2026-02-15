package rs.getgo.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.rating.CreateRatingDTO;
import rs.getgo.backend.dtos.rating.CreatedRatingDTO;
import rs.getgo.backend.dtos.rating.GetRatingDTO;
import rs.getgo.backend.exceptions.RatingException;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.entities.Rating;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.repositories.PassengerRepository;
import rs.getgo.backend.repositories.RatingRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final DriverRepository driverRepository;
    private final PassengerRepository passengerRepository;
    private final CompletedRideRepository completedRideRepository;

    public RatingServiceImpl(
            RatingRepository ratingRepository,
            DriverRepository driverRepository,
            PassengerRepository passengerRepository,
            CompletedRideRepository completedRideRepository
    ) {
        this.ratingRepository = ratingRepository;
        this.driverRepository = driverRepository;
        this.passengerRepository = passengerRepository;
        this.completedRideRepository = completedRideRepository;
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
    public boolean hasUserRatedRide(Long passengerId, Long rideId) {
        return ratingRepository.existsByPassenger_IdAndCompletedRide_Id(passengerId, rideId);
    }

    @Override
    public List<GetRatingDTO> getRatingsByDriver(Long driverId) {
        List<Rating> ratings = ratingRepository
                .findByCompletedRide_DriverIdOrderByIdDesc(driverId);

        return ratings.stream()
                .map(this::mapToGetDTO)
                .toList();
    }

    @Override
    public CreatedRatingDTO createRating(CreateRatingDTO dto, Long rideId, Authentication auth) {

        String email = auth.getName();

        Passenger passenger = passengerRepository.findByEmail(email)
                .orElseThrow(() -> new RatingException("PASSENGER_NOT_FOUND", "Passenger not found"));

        CompletedRide ride = completedRideRepository.findById(rideId)
                .orElseThrow(() -> new RatingException("RIDE_NOT_FOUND", "Ride not found"));

        if (ride.getEndTime() == null) {
            throw new RatingException("RIDE_NOT_FINISHED", "Cannot rate unfinished ride");
        }

        if (ride.getEndTime().plusDays(3).isBefore(LocalDateTime.now())) {
            throw new RatingException("EXPIRED", "Rating expired");
        }

        if (hasUserRatedRide(passenger.getId(), ride.getId())) {
            throw new RatingException("ALREADY_RATED", "Ride already rated");
        }

        Rating rating = new Rating();
        rating.setCompletedRide(ride);
        rating.setPassenger(passenger);
        rating.setDriverRating(dto.getDriverRating());
        rating.setVehicleRating(dto.getVehicleRating());
        rating.setComment(dto.getComment());

        Rating saved = ratingRepository.save(rating);

        Driver driver = driverRepository.findById(ride.getDriverId()).orElse(null);
        Long vehicleId = driver != null && driver.getVehicle() != null
                ? driver.getVehicle().getId()
                : null;

        return new CreatedRatingDTO(
                saved.getId(),
                passenger.getId(),
                ride.getId(),
                ride.getDriverId(),
                vehicleId,
                saved.getDriverRating(),
                saved.getVehicleRating(),
                saved.getComment()
        );
    }



}
