package controllers;

import dtos.rating.CreateRatingDTO;
import dtos.rating.CreatedRatingDTO;
import dtos.rating.GetRatingDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetRatingDTO>> getRatings() {
        Collection<GetRatingDTO> ratings = new ArrayList<>() ;


        return new ResponseEntity<Collection<GetRatingDTO>>(ratings, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRatingDTO> getRating(@PathVariable("id") Long id) {
        GetRatingDTO rating = new GetRatingDTO();

        return new ResponseEntity<GetRatingDTO>(rating, HttpStatus.OK);
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedRatingDTO> createRating(@RequestBody CreateRatingDTO rating) throws Exception {
        CreatedRatingDTO savedRating = new CreatedRatingDTO();

        return new ResponseEntity<CreatedRatingDTO>(savedRating, HttpStatus.CREATED);
    }
}
