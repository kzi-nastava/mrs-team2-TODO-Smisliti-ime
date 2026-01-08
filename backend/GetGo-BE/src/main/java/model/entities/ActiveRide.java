package model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.enums.RideStatus;
import model.enums.VehicleType;

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
}