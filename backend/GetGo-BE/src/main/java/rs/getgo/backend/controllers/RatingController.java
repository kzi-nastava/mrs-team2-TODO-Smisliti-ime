package rs.getgo.backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import rs.getgo.backend.dtos.rating.CreateRatingDTO;
import rs.getgo.backend.dtos.rating.CreatedRatingDTO;
import rs.getgo.backend.dtos.rating.GetRatingDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    // 2.8 Vehicle and driver rating
    @PreAuthorize("hasRole('PASSENGER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetRatingDTO>> getRatings() {
        Collection<GetRatingDTO> ratings = new ArrayList<>() ;

        GetRatingDTO rating1 = new GetRatingDTO(1L, 101L, 10L, 1001L, 501L, 5, 4, "Great ride!");
        GetRatingDTO rating2 = new GetRatingDTO(2L, 102L, 11L, 1002L, 502L, 4, 5, "Nice driver!");

        ratings.add(rating1);
        ratings.add(rating2);

        return new ResponseEntity<Collection<GetRatingDTO>>(ratings, HttpStatus.OK);
    }

    // 2.8 Vehicle and driver rating
    @PreAuthorize("hasRole('PASSENGER')")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRatingDTO> getRating(@PathVariable("id") Long id) {

        GetRatingDTO rating = new GetRatingDTO(id,101L, 10L, 1001L, 501L, 5, 4, "Great ride!");

        return new ResponseEntity<GetRatingDTO>(rating, HttpStatus.OK);
    }

    // 2.8 Vehicle and driver rating
    @PreAuthorize("hasRole('PASSENGER')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedRatingDTO> createRating(@RequestBody CreateRatingDTO rating) throws Exception {
//        CreatedRatingDTO savedRating = new CreatedRatingDTO();

        CreatedRatingDTO savedRating = new CreatedRatingDTO(3L, 103L, 12L, 1003L, rating.getDriverRating(), rating.getVehicleRating(), rating.getComment());
        return new ResponseEntity<CreatedRatingDTO>(savedRating, HttpStatus.CREATED);
    }
}
