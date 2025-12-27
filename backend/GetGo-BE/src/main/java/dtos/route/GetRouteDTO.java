package dtos.route;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetRouteDTO {
    private Long id;
    private String startingPoint;
    private String endingPoint;
    private Double distance;
    private Double estimatedTime;
}
