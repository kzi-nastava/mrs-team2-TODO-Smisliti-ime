package rs.getgo.backend.dtos.favorite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class GetFavoriteRideDTO {
    private Long id;
    private List<String> addresses;
    private List<Double> latitudes;
    private List<Double> longitudes;
    private String vehicleType;
    private boolean needsBabySeats;
    private boolean needsPetFriendly;
    private List<String> linkedPassengerEmails;
}
