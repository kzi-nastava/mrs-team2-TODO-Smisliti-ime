package rs.getgo.backend.dtos.ride;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.dtos.passenger.GetRidePassengerDTO;
import rs.getgo.backend.model.entities.Route;
import rs.getgo.backend.model.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetReorderRideDTO {
    private Long id;
    private Long driverId;
    private List<GetRidePassengerDTO> passengers;
    private String startPoint;
    private String endPoint;
    private LocalDateTime startingTime;
    private LocalDateTime finishedTime;
    private Integer duration;
    private Boolean isCancelled;
    private Boolean isFavourite;
    private String status; // it can be ACTIVE, FINISHED, CANCELLED, SCHEDULED
    private Double price;
    private Boolean panicActivated;
    private VehicleType vehicleType;
    private Boolean needsBabySeats;
    private Boolean needsPetFriendly;
    private Route route;
    private Double estDistance;
    private Double estTime;
    private String cancelReason;
    private String cancelledBy;
}
