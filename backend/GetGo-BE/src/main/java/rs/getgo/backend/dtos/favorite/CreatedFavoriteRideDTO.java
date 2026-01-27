package rs.getgo.backend.dtos.favorite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CreatedFavoriteRideDTO {
    private Long favoriteRideId;
    private boolean success;
}
