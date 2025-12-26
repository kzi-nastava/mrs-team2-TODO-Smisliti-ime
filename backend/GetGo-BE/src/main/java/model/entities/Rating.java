package model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer driverRating;  // 1-5
    private Integer vehicleRating; // 1-5

    private String comment;

    @ManyToOne
    private Passenger passenger;

    @ManyToOne
    private Ride ride;
}