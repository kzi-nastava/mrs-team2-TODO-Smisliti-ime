package model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.enums.VehicleType;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    private String licensePlate;
    private int numberOfSeats;
    private boolean babyDriver;
    private boolean petFriendly;

    @ManyToOne
    private Driver driver;
}
