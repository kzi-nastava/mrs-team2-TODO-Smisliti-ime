package rs.getgo.backend.dtos.activeRide;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetActiveRideAdminDTO {
    private Long id;

    private Long driverId;
    private String driverName;
    private String driverEmail;

    private String startPoint;
    private String endPoint;

    private LocalDateTime scheduledTime;
    private LocalDateTime actualStartTime;

    private RideStatus status;

    private VehicleType vehicleType;
    private double estimatedPrice;
    private double estimatedDurationMin;
}
