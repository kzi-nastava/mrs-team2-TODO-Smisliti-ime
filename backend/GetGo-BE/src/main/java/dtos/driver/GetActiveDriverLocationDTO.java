package dtos.driver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetActiveDriverLocationDTO {
    private Long driverId;
    private Double latitude;
    private Double longitude;
    private String vehicleType;
}
