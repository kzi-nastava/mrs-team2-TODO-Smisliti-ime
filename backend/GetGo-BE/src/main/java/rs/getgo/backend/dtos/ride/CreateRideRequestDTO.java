package rs.getgo.backend.dtos.ride;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateRideRequestDTO {
    private List<Double> latitudes;
    private List<Double> longitudes;
    private List<String> addresses;

    private String scheduledTime;     // "HH:mm" format or null for immediate
    private List<String> friendEmails;
    private Boolean hasBaby;
    private Boolean hasPets;
    private String vehicleType;
}
