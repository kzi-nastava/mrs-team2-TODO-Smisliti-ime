package model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startingTime;
    private int duration;
    private boolean isCancelled;
    private boolean isFavourite;

    @ManyToOne
    private Driver driver;

    @ManyToOne
    private Passenger passenger;

    @ManyToOne
    private Route route;
}