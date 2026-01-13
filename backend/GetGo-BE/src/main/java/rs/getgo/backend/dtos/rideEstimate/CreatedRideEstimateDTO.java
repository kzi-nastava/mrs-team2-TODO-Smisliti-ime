package rs.getgo.backend.dtos.rideEstimate;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class CreatedRideEstimateDTO {
    private Double price;
    private Integer durationMinutes;
    private Double distanceKm;
    private String breakdown; // e.g. "1->2: 0.85 km, 2 min; 2->3: 1.75 km, 5 min; Total: 7 min"

    public CreatedRideEstimateDTO(Double price, Integer durationMinutes, Double distanceKm) {
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
    }

    public CreatedRideEstimateDTO(Double price, Integer durationMinutes, Double distanceKm, String breakdown) {
        this(price, durationMinutes, distanceKm);
        this.breakdown = breakdown;
    }
}