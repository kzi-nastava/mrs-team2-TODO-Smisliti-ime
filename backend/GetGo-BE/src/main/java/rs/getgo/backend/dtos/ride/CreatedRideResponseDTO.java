package rs.getgo.backend.dtos.ride;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedRideResponseDTO {
    private String status;
    private String message;
    private Long rideId; // Present if status is success
}
