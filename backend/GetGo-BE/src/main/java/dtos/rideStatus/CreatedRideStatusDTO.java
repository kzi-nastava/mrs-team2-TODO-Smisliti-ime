package dtos.rideStatus;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedRideStatusDTO {
    private Long rideId;
    private String status;
}
