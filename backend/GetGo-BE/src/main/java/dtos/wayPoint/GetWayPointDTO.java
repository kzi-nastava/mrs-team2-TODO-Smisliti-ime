package dtos.wayPoint;

import dtos.wayPoint.GetWayPointDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetWayPointDTO {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String address;
}
