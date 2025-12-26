package dtos.ride;

import dtos.route.GetRouteDTO;
import dtos.wayPoint.GetWayPointDTO;

import java.time.LocalDateTime;
import dtos.wayPoint.GetWayPointDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetRideDTO {
    private Long id;
    private Long driverId;
    private Long passengerId;
    private LocalDateTime startingTime;
    private Integer duration;
    private Boolean isCancelled;
    private Boolean isFavourite;
    private String status; // it can be ACTIVE, FINISHED, CANCELLED, SCHEDULED
    private GetWayPointDTO currentLocation;
    private GetRouteDTO route;
}
