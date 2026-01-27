package rs.getgo.backend.dtos.ride;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetDriverActiveRideDTO {
    private Long rideId;
    private String startingPoint;
    private String endingPoint;
    private Double estimatedPrice;
    private Double estimatedTimeMin;
    private String passengerName;
    private Integer passengerCount;
    private String status;
    private List<Double> latitudes;
    private List<Double> longitudes;
    private List<String> addresses;
    private LocalDateTime scheduledTime; // Null if not scheduled
}