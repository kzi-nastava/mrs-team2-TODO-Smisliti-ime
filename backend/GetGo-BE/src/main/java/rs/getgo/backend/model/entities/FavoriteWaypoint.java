package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Waypoint that doesn't track time reached
 */
@Entity
@Table(name = "favorite_waypoints")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class FavoriteWaypoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private Double latitude;
    private Double longitude;
}
