package rs.getgo.backend.dtos.ride;

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
    private String startAddress;
    private String destinationAddress;
    private Double estimatedTime; // minutes

    public GetRideTrackingDTO(Long id, double v, double v1, double v2) {
    }
}