package dtos.ride;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedRideDTO {
    private Long rideId;
    private String status;
    private String message;

    // If accepted
    private Long driverId;
    private String driverName;
    private Double estimatedPrice;
    private Integer estimatedDuration; // in minutes
    private LocalDateTime scheduledTime; // non-null if scheduled

    // If rejected
    private String rejectionReason;
}
