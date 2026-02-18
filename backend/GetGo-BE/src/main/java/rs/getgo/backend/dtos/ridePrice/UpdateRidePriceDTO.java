package rs.getgo.backend.dtos.ridePrice;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRidePriceDTO {

    @NotNull(message = "Price per km is required")
    @Min(value = 0, message = "Price per km must be positive")
    private Double pricePerKm;

    @NotNull(message = "Start price is required")
    @Min(value = 0, message = "Start price must be positive")
    private Double startPrice;
}
