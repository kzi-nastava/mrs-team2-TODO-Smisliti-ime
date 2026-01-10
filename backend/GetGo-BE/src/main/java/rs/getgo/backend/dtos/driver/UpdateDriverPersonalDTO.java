package rs.getgo.backend.dtos.driver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateDriverPersonalDTO {
    private String name;
    private String surname;
    private String phone;
    private String address;
}
