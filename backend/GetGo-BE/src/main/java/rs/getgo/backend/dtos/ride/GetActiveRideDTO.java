package rs.getgo.backend.dtos.ride;

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
public class GetActiveRideDTO {
    private Long id;
    private String startingPoint;
    private String endingPoint;
    private List<String> waypointAddresses;

    private String driverEmail;
    private String driverName;
    private String payingPassengerEmail;
    private List<String> linkedPassengerEmails;

    private Double estimatedPrice;
    private Double setEstimatedDurationMin;
    private LocalDateTime scheduledTime;
    private LocalDateTime actualStartTime;
    private String status;
    private String vehicleType;
    private Boolean needsBabySeats;
    private Boolean needsPetFriendly;
}
