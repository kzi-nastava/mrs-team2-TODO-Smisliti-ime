package model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.enums.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name="profile_change_requests")
@NoArgsConstructor @AllArgsConstructor
public class ProfileChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String newEmail;
    private String newFirstName;
    private String newLastName;
    private String newAddress;
    private String newPhoneNumber;
    private String newProfilePicture;

    private String newVehicleModel;
    private String newVehicleLicensePlate;
    private Integer newVehicleSeats;
    private Boolean newVehicleBabyFriendly;
    private Boolean newVehiclePetFriendly;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;

    @ManyToOne
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_admin_id")
    private Administrator reviewedBy;
}