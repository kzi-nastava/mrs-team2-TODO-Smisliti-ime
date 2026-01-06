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
public class CreateRideDTO {
    private String startPoint;
    private String endPoint;
    private List<String> intermediateStops;

    private String vehicleType;
    private Boolean allowsBabies;
    private Boolean allowsPets;

    private List<String> passengerEmails;

    private LocalDateTime scheduledTime; // has value if scheduled
}
