package rs.getgo.backend.dtos.ride;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RideCompletionDTO {
    private Long rideId;
    private String status;
    private double price;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMinutes;
}