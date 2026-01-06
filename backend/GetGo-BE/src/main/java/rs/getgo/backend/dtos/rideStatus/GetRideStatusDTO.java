package rs.getgo.backend.dtos.rideStatus;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetRideStatusDTO {
    private Long rideId;
    private String status;
}