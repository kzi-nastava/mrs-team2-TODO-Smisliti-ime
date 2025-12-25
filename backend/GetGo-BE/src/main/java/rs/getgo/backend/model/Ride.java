package rs.getgo.backend.model;

import lombok.Getter;
import lombok.Setter;
import rs.getgo.backend.enums.RideStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class Ride {
    private Long id;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
    private Integer duration; //? in minutes
    private Double price;
    private Boolean isPaid;

    private RideStatus status;
    private Boolean isFavorite;

    private WayPoint currentLocation;
    private Route route;
}
