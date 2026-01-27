package rs.getgo.backend.dtos.ride;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelRideRequestDTO {
//    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
