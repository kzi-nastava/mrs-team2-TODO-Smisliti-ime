package dtos.ride;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetRideTrackingDTO {
    private Long rideId;
    private Double vehicleLatitude;
    private Double vehicleLongitude;
    private Double estimatedTime; // minutes
}
