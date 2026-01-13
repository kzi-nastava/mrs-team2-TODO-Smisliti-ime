package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.getgo.backend.model.enums.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name="vehicle_change_requests")
@NoArgsConstructor @AllArgsConstructor
public class VehicleChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestedVehicleModel;
    private String requestedVehicleType;
    private String requestedVehicleLicensePlate;
    private Integer requestedVehicleSeats;
    private Boolean requestedVehicleHasBabySeats;
    private Boolean requestedVehicleAllowsPets;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_admin_id")
    private Administrator reviewedBy;

    private String rejectionReason;
}