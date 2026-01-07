package model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.enums.RideStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@Table(name="rides")
@NoArgsConstructor @AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime; // Value is assigned after ride ends
    private int estPrice;
    private int actualPrice; // Value is assigned after ride ends
    private int estDuration;
    private int duration; // Value is assigned after ride ends
    private int estDistance;

    private boolean isCancelled;
    private String cancelReason; // Value is assigned when ride is being canceled
    @ManyToOne
    @JoinColumn(name = "cancelled_by_user_id")
    private User cancelledBy;  // Driver or passenger

    private boolean isFavourite;

    private boolean isPanicPressed;

    @Enumerated(EnumType.STRING)
    private RideStatus rideStatus;

    @ManyToOne
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "paying_passenger_id")
    private Passenger payingPassenger;

    @ManyToMany
    @JoinTable(
            name = "ride_passengers",
            joinColumns = @JoinColumn(name = "ride_id"),
            inverseJoinColumns = @JoinColumn(name = "passenger_id")
    )
    private List<Passenger> sidePassengers;

    @ManyToOne
    private Route route;

    @OneToOne
    private WayPoint currentLocation;
}