package dtos.route;

import dtos.wayPoint.GetWayPointDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetRouteDTO {
    private Long id;
    private String startingPoint;
    private String endingPoint;
    private Double distance;
    private Double estimatedTime;
}
