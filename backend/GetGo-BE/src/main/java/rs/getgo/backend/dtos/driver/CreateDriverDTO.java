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
public class CreateDriverDTO {
    // Personal info
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Surname is required")
    @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
    private String surname;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    private String phone;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    private String address;

    // Vehicle info
    @NotBlank(message = "Vehicle model is required")
    @Size(min = 2, max = 50, message = "Vehicle model must be between 2 and 50 characters")
    private String vehicleModel;

    @NotBlank(message = "Vehicle type is required")
    @Pattern(regexp = "^(SEDAN|SUV|VAN|LUXURY)$", message = "Vehicle type must be SEDAN, SUV, VAN, or LUXURY")
    private String vehicleType;

    @NotBlank(message = "Vehicle license plate is required")
    @Pattern(regexp = "^[A-Z0-9]{5,10}$", message = "License plate must be 5-10 alphanumeric characters")
    private String vehicleLicensePlate;

    @NotNull(message = "Vehicle seats is required")
    @Min(value = 1, message = "Vehicle must have at least 1 seat")
    @Max(value = 100, message = "Vehicle cannot have more than 100 seats")
    private Integer vehicleSeats;

    @NotNull(message = "Vehicle baby seats availability is required")
    private Boolean vehicleHasBabySeats;

    @NotNull(message = "Vehicle pet-friendliness is required")
    private Boolean vehicleAllowsPets;
}