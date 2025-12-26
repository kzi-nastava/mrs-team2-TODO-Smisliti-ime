package dtos.vehicle;

import dtos.wayPoint.GetWayPointDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetVehicleDTO {
    private Long id;
    private String model;
    private String type;
    private String licensePlate;
    private Integer numberOfSeats;
    private Boolean babyDriver;
    private Boolean petFriendly;
    private Long driverId;
    private GetWayPointDTO currentLocation;
    private Boolean isAvailable;
}
