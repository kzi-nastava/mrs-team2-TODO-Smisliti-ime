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
@Table(name="personal_change_requests")
@NoArgsConstructor @AllArgsConstructor
public class PersonalChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestedName;
    private String requestedSurname;
    private String requestedAddress;
    private String requestedPhone;

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