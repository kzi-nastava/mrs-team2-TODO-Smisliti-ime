package dtos.wayPoint;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetWayPointDTO {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String address;
}
