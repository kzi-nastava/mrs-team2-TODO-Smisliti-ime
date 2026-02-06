package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@Table(name="completed_rides")
@NoArgsConstructor @AllArgsConstructor
public class CompletedRide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "route_id")
    private Route route;

    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private double estimatedPrice;
    private double actualPrice;

    private double estDistanceKm;
    private double actualDistanceKm;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
    private String vehicleModel;
    private String vehicleLicensePlate;

    private boolean needsBabySeats;
    private boolean needsPetFriendly;

    private Long driverId;
    private String driverName;
    private String driverEmail;

    private Long payingPassengerId;
    private String payingPassengerName;
    private String payingPassengerEmail;

    @ElementCollection
    @CollectionTable(name = "completed_ride_passengers", joinColumns = @JoinColumn(name = "ride_id"))
    private List<Long> linkedPassengerIds;

    private boolean isCompletedNormally;

    private boolean isPanicPressed;
    private LocalDateTime panicPressedAt;

    private boolean isCancelled;
    private String cancelReason;
    private Long cancelledByUserId; // Drive can be canceled by both driver and passenger

    private boolean isStoppedEarly;

    @ManyToOne
    @JoinColumn(name = "early_stop_location_id")
    private WayPoint earlyStopLocation;
}