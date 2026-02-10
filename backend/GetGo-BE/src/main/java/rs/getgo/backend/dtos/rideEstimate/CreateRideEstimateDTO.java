package rs.getgo.backend.dtos.rideEstimate;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateRideEstimateDTO {
    // getters and setters
    private List<Coordinate> coordinates; // List of coordinates: start, destination
    @Setter
    @Getter
    public static class Coordinate {
        private Double lat;
        private Double lng;

    }
}