package rs.getgo.backend.dtos.driver;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateDriverVehicleDTO {
    @NotBlank(message = "Vehicle model is required")
    @Size(min = 2, max = 50, message = "Vehicle model must be between 2 and 50 characters")
    private String vehicleModel;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotBlank(message = "Vehicle license plate is required")
    @Pattern(regexp = "^[A-Z0-9]{5,10}$", message = "License plate must be 5-10 alphanumeric characters")
    private String vehicleLicensePlate;

    @NotNull(message = "Vehicle seats is required")
    @Min(value = 2, message = "Vehicle must have at least 2 seats")
    @Max(value = 100, message = "Vehicle cannot have more than 8 seats")
    private Integer vehicleSeats;

    @NotNull(message = "Vehicle baby seats availability is required")
    private Boolean vehicleHasBabySeats;

    @NotNull(message = "Vehicle pet-friendliness is required")
    private Boolean vehicleAllowsPets;
}