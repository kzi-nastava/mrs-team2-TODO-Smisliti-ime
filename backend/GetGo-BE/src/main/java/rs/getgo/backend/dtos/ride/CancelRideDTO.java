package rs.getgo.backend.dtos.ride;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CancelRideDTO {
    private Long cancelerId;
    private String role; // "DRIVER" or "PASSENGER"
    private String reason;
    private LocalDateTime scheduledStartTime; // optional; needed to validate passenger cancellations
    private Boolean passengersEntered; // optional; needed to validate driver cancellations

}

