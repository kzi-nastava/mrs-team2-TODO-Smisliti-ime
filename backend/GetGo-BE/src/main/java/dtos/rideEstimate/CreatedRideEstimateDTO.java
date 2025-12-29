package dtos.rideEstimate;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedRideEstimateDTO {
    private Double price;
    private Integer durationMinutes;
    private Double distanceKm;
}