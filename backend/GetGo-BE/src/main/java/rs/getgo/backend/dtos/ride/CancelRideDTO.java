package rs.getgo.backend.dtos.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CancelRideDTO {
    private Long cancelerId;
    private String role; // "DRIVER" or "PASSENGER"

    private String reason;

    private Boolean passengersEntered;
    private LocalDateTime scheduledStartTime;
}
