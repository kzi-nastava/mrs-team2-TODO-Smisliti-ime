package dtos.ride;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetRideTrackingDTO {
    private Long rideId;
    private Double vehicleLatitude;
    private Double vehicleLongitude;
    private Double estimatedTime; // minutes
}
