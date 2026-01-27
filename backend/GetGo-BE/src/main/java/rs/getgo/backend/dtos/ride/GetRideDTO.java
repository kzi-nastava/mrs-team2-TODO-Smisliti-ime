package rs.getgo.backend.dtos.ride;

import rs.getgo.backend.dtos.passenger.GetPassengerDTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import rs.getgo.backend.dtos.passenger.GetRidePassengerDTO;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetRideDTO {
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
}
