package rs.getgo.backend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Route {
    private WayPoint startLocation;
    private WayPoint endLocation;
    private Double distance; // in kilometers
    private Integer estimatedTime; // in minutes
}
