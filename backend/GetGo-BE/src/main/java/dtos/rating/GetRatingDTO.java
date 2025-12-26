package dtos.rating;

import dtos.wayPoint.GetWayPointDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetRatingDTO {
    private Long id;
    private Long rideId;
    private Long driverId;
    private Long vehicleId;
    private Long passengerId;
    private Integer driverRating;
    private Integer vehicleRating;
    private String comment;
}

