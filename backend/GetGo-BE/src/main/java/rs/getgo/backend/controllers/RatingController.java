package rs.getgo.backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import rs.getgo.backend.dtos.rating.CreateRatingDTO;
import rs.getgo.backend.dtos.rating.CreatedRatingDTO;
import rs.getgo.backend.dtos.rating.GetRatingDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.services.RatingService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private CompletedRideRepository rideRepository;

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
    public ResponseEntity<CreatedRatingDTO> createRating(@RequestBody CreateRatingDTO dto, @RequestParam Long rideId) throws Exception {

        CompletedRide ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new Exception("Ride not found with id: " + rideId));

        CreatedRatingDTO savedRating = ratingService.create(dto, ride);

        return new ResponseEntity<CreatedRatingDTO>(savedRating, HttpStatus.CREATED);
    }
}
