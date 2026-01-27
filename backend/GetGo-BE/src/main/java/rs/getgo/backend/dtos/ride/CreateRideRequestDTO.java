package rs.getgo.backend.dtos.ride;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
//    @NotNull
//    @Size(min = 2)
    private List<Double> latitudes;

//    @NotNull
//    @Size(min = 2)
    private List<Double> longitudes;


    private List<String> addresses;

//    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$")
    private String scheduledTime;     // "HH:mm" format or null for immediate

//    private List<@Email String> friendEmails;
    private List<String> friendEmails;
    private Boolean hasBaby;
    private Boolean hasPets;
    private String vehicleType;
}
