package dtos.favorite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class CreatedFavoriteDTO {
    private Long userId;
    private Long rideId;
    private LocalDateTime createdAt;
}
