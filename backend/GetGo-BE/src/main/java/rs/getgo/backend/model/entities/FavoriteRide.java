package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.VehicleType;

import java.util.List;

@Entity
@Getter @Setter
@Table(name="favorite_rides")
@NoArgsConstructor @AllArgsConstructor
public class FavoriteRide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
    private boolean needsBabySeats;
    private boolean needsPetFriendly;

    @ElementCollection
    @CollectionTable(name = "favorite_ride_passengers", joinColumns = @JoinColumn(name = "favorite_ride_id"))
    private List<String> linkedPassengerEmails;
}