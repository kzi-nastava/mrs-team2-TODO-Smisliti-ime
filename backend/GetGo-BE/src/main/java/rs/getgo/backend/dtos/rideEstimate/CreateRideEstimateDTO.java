package rs.getgo.backend.dtos.rideEstimate;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateRideEstimateDTO {
    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;
    private String vehicleType;
}