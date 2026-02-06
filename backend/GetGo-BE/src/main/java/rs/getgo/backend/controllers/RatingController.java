package rs.getgo.backend.controllers;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import rs.getgo.backend.dtos.rating.CreateRatingDTO;
import rs.getgo.backend.dtos.rating.CreatedRatingDTO;
import rs.getgo.backend.dtos.rating.GetRatingDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.repositories.PassengerRepository;
import rs.getgo.backend.services.RatingService;
import org.springframework.security.core.Authentication;
import rs.getgo.backend.utils.RatingTokenData;


import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final CompletedRideRepository rideRepository;
    private final PassengerRepository passengerRepository;

    public RatingController(
            RatingService ratingService,
            CompletedRideRepository rideRepository,
            PassengerRepository passengerRepository
    ) {
        this.ratingService = ratingService;
        this.rideRepository = rideRepository;
        this.passengerRepository = passengerRepository;
    }

    // 2.8 Vehicle and driver rating
    @PreAuthorize("hasRole('PASSENGER') or hasRole('ADMIN')")
    @GetMapping(value="/ride/{rideId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetRatingDTO>> getRatingsByRide(@PathVariable Long rideId) {

        List<GetRatingDTO> ratings = ratingService.getRatingsByRide(rideId);

        return new ResponseEntity<>(ratings, HttpStatus.OK);
    }

    // 2.8 Vehicle and driver rating
    @PreAuthorize("hasRole('PASSENGER')")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRatingDTO> getRating(@PathVariable("id") Long id) {

        GetRatingDTO rating = new GetRatingDTO(id,101L, 10L, 1001L, 501L, 5, 4, "From controller mistake!");

        return new ResponseEntity<GetRatingDTO>(rating, HttpStatus.OK);
    }

    // 2.8 Vehicle and driver rating
    @PreAuthorize("hasRole('PASSENGER')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedRatingDTO> createRating(@Valid @RequestBody CreateRatingDTO dto, @RequestParam Long rideId, Authentication auth) throws Exception {

        CompletedRide ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new Exception("Ride not found with id: " + rideId));

        String email = auth.getName();
        Long passengerId = passengerRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Passenger not found"))
                .getId();


        CreatedRatingDTO savedRating = ratingService.create(dto, ride, passengerId);

        return new ResponseEntity<CreatedRatingDTO>(savedRating, HttpStatus.CREATED);
    }

    @GetMapping("/debug-auth")
    public Object debugAuth(Authentication auth) {
        return auth;
    }

    @GetMapping("/driver/{driverId}")
    public List<GetRatingDTO> getRatingsByDriver(@PathVariable Long driverId) {
        return ratingService.getRatingsByDriver(driverId);
    }
}
