package rs.getgo.backend.controllers;

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
import rs.getgo.backend.utils.TokenUtils;


import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final CompletedRideRepository rideRepository;
    private final TokenUtils tokenUtils;
    private final PassengerRepository passengerRepository;

    public RatingController(
            RatingService ratingService,
            CompletedRideRepository rideRepository,
            TokenUtils tokenUtils,
            PassengerRepository passengerRepository
    ) {
        this.ratingService = ratingService;
        this.rideRepository = rideRepository;
        this.tokenUtils = tokenUtils;
        this.passengerRepository = passengerRepository;
    }

    // 2.8 Vehicle and driver rating
    @PreAuthorize("hasRole('PASSENGER')")
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
    public ResponseEntity<CreatedRatingDTO> createRating(@RequestBody CreateRatingDTO dto, @RequestParam Long rideId, Authentication auth) throws Exception {

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

    @PostMapping(value = "/rate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> rateRideWithToken(@RequestParam String token, @RequestBody CreateRatingDTO dto) {
        try {
            RatingTokenData data = tokenUtils.parseRatingToken(token);

            CompletedRide ride = rideRepository.findById(data.getRideId())
                    .orElseThrow(() -> new RuntimeException("Ride not found"));

            boolean alreadyRated = ratingService.hasUserRatedRide(data.getPassengerId(), data.getRideId());
            if (alreadyRated) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You have already rated this ride");
            }

            CreatedRatingDTO savedRating = ratingService.create(dto, ride, data.getPassengerId());

            return ResponseEntity.ok("Rating submitted successfully!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid or expired token");
        }
    }

}
