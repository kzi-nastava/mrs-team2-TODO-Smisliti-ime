package rs.getgo.backend.dtos.rating;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateRatingDTO {
    private Integer driverRating;
    private Integer vehicleRating;
    private String comment;
}
