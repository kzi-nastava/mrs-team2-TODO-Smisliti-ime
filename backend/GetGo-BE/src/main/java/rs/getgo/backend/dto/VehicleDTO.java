package rs.getgo.backend.dto;

import lombok.Getter;
import lombok.Setter;
import rs.getgo.backend.model.WayPoint;

@Getter
@Setter
public class VehicleDTO {
    private Long id;
    private String type;
    private WayPoint currentLocation;
    private Boolean available;
}
