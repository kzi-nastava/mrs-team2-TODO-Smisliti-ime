package rs.getgo.backend.dtos.ridePrice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetRidePriceDTO {
    private Double pricePerKm;
    private Double startPrice;
}
