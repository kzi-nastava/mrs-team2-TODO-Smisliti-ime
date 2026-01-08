package rs.getgo.backend.dtos.admin;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateAdminDTO {
    private String name;
    private String surname;
    private String phone;
    private String address;
}
