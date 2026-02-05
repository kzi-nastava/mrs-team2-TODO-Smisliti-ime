package rs.getgo.backend.dtos.ride;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetRideFinishedDTO {
    private Long rideId;
    private String status;
    private Double price;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMinutes;
}