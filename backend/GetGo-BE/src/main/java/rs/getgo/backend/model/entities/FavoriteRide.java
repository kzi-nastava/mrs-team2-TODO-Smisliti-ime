package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.VehicleType;

import java.util.ArrayList;
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

    @Column(name = "source_completed_ride_id", nullable = false)
    private Long completedRideId; // Completed ride the favorite binds to

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "favorite_ride_id")
    @OrderColumn(name = "waypoint_order")
    private List<FavoriteWaypoint> waypoints = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    private boolean needsBabySeats;
    private boolean needsPetFriendly;

    @ElementCollection
    @CollectionTable(name = "favorite_ride_passengers", joinColumns = @JoinColumn(name = "favorite_ride_id"))
    private List<String> linkedPassengerEmails;
}