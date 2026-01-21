package rs.getgo.backend.dtos.driver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateDriverLocationDTO {
    private Double latitude;
    private Double longitude;
}
