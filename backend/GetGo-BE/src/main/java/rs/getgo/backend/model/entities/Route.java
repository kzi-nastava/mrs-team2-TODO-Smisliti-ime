package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name="routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String startingPoint;
    private String endingPoint;

    private double estTimeMin;
    private double estDistanceKm;

    // Encoded polyline (from Google Maps API for drawing route on map)
    @Column(length = 10000)
    private String encodedPolyline;

    // Ordered waypoints
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "route_id")
    @OrderColumn(name = "waypoint_order")
    private List<WayPoint> waypoints = new ArrayList<>();
}