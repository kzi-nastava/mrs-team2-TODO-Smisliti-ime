package rs.getgo.backend.dtos.driver;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetDriverChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String driverEmail;
    private String driverName;

    // Current data
    private String currentName;
    private String currentSurname;
    private String currentPhone;
    private String currentAddress;
    private String currentProfilePictureUrl;
    private String currentVehicleModel;
    private String currentVehicleType;
    private String currentVehicleLicensePlate;

    // Requested changes
    private String requestedName;
    private String requestedSurname;
    private String requestedPhone;
    private String requestedAddress;
    private String requestedProfilePictureUrl;
    private String requestedVehicleModel;
    private String requestedVehicleType;
    private String requestedVehicleLicensePlate;

    private String status; // Request status
    private LocalDateTime createdAt;
}
