package rs.getgo.backend.dtos.driver;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetDriverDTO {
    // Personal info
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String phone;
    private String address;
    private String profilePictureUrl;
    private Integer recentHoursWorked;
    // Vehicle info
    private String vehicleModel;
    private String vehicleType;
    private String vehicleLicensePlate;
    private Integer vehicleSeats;
    private Boolean vehicleHasBabySeats;
    private Boolean vehicleAllowsPets;
}
