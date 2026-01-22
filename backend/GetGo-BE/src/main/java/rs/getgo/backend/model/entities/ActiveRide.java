package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@Table(name="active_rides")
@NoArgsConstructor @AllArgsConstructor
public class ActiveRide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "current_location_id")
    private WayPoint currentLocation;

    private LocalDateTime scheduledTime;
    private LocalDateTime actualStartTime;

    private double estimatedPrice;
    private double estimatedDurationMin;

    @Enumerated(EnumType.STRING)
    private RideStatus status;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
    private boolean needsBabySeats;
    private boolean needsPetFriendly;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "paying_passenger_id", nullable = false)
    private Passenger payingPassenger;

    @ManyToMany
    @JoinTable(
            name = "active_ride_passengers",
            joinColumns = @JoinColumn(name = "ride_id"),
            inverseJoinColumns = @JoinColumn(name = "passenger_id")
    )
    private List<Passenger> linkedPassengers;

    // Which route waypoint is next (0=start point, 1=first waypoint...)
    private Integer targetWaypointIndex = 0;

    // Detailed path with coords between waypoints
    @Column(columnDefinition = "TEXT")
    private String movementPathJson;

    // Current coord in movementPathJson
    private Integer currentPathIndex = 0;
}