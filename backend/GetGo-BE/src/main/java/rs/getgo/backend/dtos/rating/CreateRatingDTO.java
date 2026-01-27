package rs.getgo.backend.dtos.rating;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateRatingDTO {

    @NotNull(message = "Driver rating is required")
    @Min(value = 1, message = "Driver rating must be between 1 and 5")
    @Max(value = 5, message = "Driver rating must be between 1 and 5")
    private Integer driverRating;

    @NotNull(message = "Vehicle rating is required")
    @Min(value = 1, message = "Vehicle rating must be between 1 and 5")
    @Max(value = 5, message = "Vehicle rating must be between 1 and 5")
    private Integer vehicleRating;

    @NotBlank(message = "Comment must not be empty")
    private String comment;

}
