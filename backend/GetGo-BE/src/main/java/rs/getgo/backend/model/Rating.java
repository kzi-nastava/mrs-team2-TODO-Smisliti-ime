package rs.getgo.backend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rating {
    private Long id;
    private Integer score; // 1-5
    private String comment;
    private Long rideId;
    private Long passengerId;
    private Long driverId;
    private Long vehicleId;
}

