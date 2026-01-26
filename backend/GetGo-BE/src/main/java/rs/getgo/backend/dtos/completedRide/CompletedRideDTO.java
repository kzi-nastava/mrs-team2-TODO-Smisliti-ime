package rs.getgo.backend.dtos.completedRide;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.VehicleType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompletedRideDTO {
    private Long id;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double estimatedPrice;
    private double actualPrice;
    private VehicleType vehicleType;
    private Long driverId;
    private Long payingPassengerId;

    public CompletedRideDTO(rs.getgo.backend.model.entities.CompletedRide ride) {
        this.id = ride.getId();
        this.scheduledTime = ride.getScheduledTime();
        this.startTime = ride.getStartTime();
        this.endTime = ride.getEndTime();
        this.estimatedPrice = ride.getEstimatedPrice();
        this.actualPrice = ride.getActualPrice();
        this.vehicleType = ride.getVehicleType();
        this.driverId = ride.getDriverId();
        this.payingPassengerId = ride.getPayingPassengerId();
    }
}
