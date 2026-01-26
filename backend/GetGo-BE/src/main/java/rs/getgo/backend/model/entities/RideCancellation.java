package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "ride_cancellations")
public class RideCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rideId;
    private Long cancelerId;
    private String role;
    @Column(length = 1000)
    private String reason;
    private LocalDateTime createdAt;

}

