package dtos.rating;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedRatingDTO {
    private Long id;
    private Long rideId;
    private Long driverId;
    private Long vehicleId;
    private Integer driverRating;
    private Integer vehicleRating;
    private String comment;
}
