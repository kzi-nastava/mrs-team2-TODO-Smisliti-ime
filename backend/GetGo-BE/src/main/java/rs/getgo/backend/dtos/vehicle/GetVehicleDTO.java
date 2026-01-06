package rs.getgo.backend.dtos.vehicle;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
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
