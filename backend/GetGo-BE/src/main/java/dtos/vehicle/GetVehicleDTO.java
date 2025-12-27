package dtos.vehicle;

import dtos.wayPoint.GetWayPointDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetVehicleDTO {
    private Long id;
    private String model;
    private String type;
    private Double latitude;
    private Double longitude;
    private Boolean isAvailable;
}
