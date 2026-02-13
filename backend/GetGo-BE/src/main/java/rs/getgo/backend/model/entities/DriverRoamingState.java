package rs.getgo.backend.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "driver_roaming_states")
@NoArgsConstructor @AllArgsConstructor
public class DriverRoamingState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "driver_id", unique = true, nullable = false)
    private Driver driver;

    @Column(columnDefinition = "TEXT")
    private String movementPathJson;

    private int currentPathIndex = 0;

    private int targetWaypointIndex = 0;
}