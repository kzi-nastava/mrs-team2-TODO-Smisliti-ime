package rs.getgo.backend.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetDriverVehicleChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String driverEmail;
    private String driverName;

    // Current vehicle data
    private String currentVehicleModel;
    private String currentVehicleType;
    private String currentVehicleLicensePlate;
    private Integer currentVehicleSeats;
    private Boolean currentVehicleHasBabySeats;
    private Boolean currentVehicleAllowsPets;

    // Requested vehicle changes
    private String requestedVehicleModel;
    private String requestedVehicleType;
    private String requestedVehicleLicensePlate;
    private Integer requestedVehicleSeats;
    private Boolean requestedVehicleHasBabySeats;
    private Boolean requestedVehicleAllowsPets;

    private String status;
    private LocalDateTime createdAt;
}
