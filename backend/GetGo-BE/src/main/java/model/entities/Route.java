package model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
@Table(name="routes")
@NoArgsConstructor @AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String startingPoint;
    private String endingPoint;
    private double estimatedTime;
    private double distance;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "route_id")
    @OrderColumn(name = "waypoint_order")  // Maintain order of waypoints
    private List<WayPoint> waypoints;

    private Double basePrice;
}