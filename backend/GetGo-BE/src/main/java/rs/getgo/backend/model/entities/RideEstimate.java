package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "ride_estimates")
public class RideEstimate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String startAddress;
    private String destinationAddress;

    private Double startLat;
    private Double startLon;
    private Double destLat;
    private Double destLon;

    private Double distanceMeters;
    private Integer estimatedTimeMinutes;
    private Double price;

    private LocalDateTime createdAt;
}

