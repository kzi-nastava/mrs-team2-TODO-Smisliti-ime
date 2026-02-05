package rs.getgo.backend.dtos.activeRide;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.dtos.passenger.GetPassengerDTO;
import rs.getgo.backend.dtos.passenger.GetRidePassengerDTO;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetActiveRideAdminDetailsDTO {
    private Long id;

    private Long driverId;
    private String driverName;
    private String driverEmail;

    private LocalDateTime actualStartTime;
    private LocalDateTime scheduledTime;

    private RideStatus status;

    private VehicleType vehicleType;
    private boolean needsBabySeats;
    private boolean needsPetFriendly;

    private double estimatedPrice;
    private double estimatedDurationMin;

    private String payingPassenger;
    private List<String> linkedPassengers;

    private String currentAddress;
}
