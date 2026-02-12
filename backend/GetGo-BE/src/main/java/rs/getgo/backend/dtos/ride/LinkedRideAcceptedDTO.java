package rs.getgo.backend.dtos.ride;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LinkedRideAcceptedDTO {
    private Long rideId;
    private String driverName;
}
