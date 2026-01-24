package rs.getgo.backend.dtos.ride;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StopRideDTO {
    private double latitude;
    private double longitude;
    private LocalDateTime stoppedAt;
}