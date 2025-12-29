package dtos.driver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateDriverDTO {
    // Personal info
    private String email;
    private String name;
    private String surname;
    private String phone;
    private String address;
    // Vehicle info
    private String vehicleModel;
    private String vehicleType;
    private String vehicleLicensePlate;
    private Integer vehicleSeats;
    private Boolean vehicleHasBabySeats;
    private Boolean vehicleAllowsPets;
}
