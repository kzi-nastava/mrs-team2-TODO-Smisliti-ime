package rs.getgo.backend.dtos.ride;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}