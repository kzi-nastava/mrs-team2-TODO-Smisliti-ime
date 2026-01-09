package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name="inconsistency_reports")
@NoArgsConstructor @AllArgsConstructor
public class InconsistencyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @ManyToOne
    @JoinColumn(name = "completed_ride_id")
    private CompletedRide completedRide;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;
}