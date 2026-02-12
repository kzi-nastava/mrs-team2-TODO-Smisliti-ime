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
    @NotNull(message = "Latitudes are required")
    @Size(min = 2, message = "At least 2 coordinates required")
    private List<Double> latitudes;

    @NotNull(message = "Longitudes are required")
    @Size(min = 2, message = "At least 2 coordinates required")
    private List<Double> longitudes;

    @NotNull(message = "Addresses are required")
    @Size(min = 2, message = "At least 2 addresses required")
    private List<String> addresses;

    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Scheduled time must be in HH:mm format")
    private String scheduledTime;

    private List<@Email(message = "Invalid email address") String> friendEmails;

    private Boolean hasBaby;
    private Boolean hasPets;

    private String vehicleType; // null for vehicle type any
}