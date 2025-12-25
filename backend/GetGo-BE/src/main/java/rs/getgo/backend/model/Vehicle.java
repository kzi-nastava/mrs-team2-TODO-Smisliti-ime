package rs.getgo.backend.model;

import lombok.Getter;
import lombok.Setter;
import rs.getgo.backend.enums.VehicleType;

@Getter
@Setter
public class Vehicle {
    private Long id;
    private Integer numberOfSeats;
    private Boolean babyDriver;
    private Boolean petFriendly;
    private VehicleType type;
    private Boolean available;
    private WayPoint currentLocation;
}
