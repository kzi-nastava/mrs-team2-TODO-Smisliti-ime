package rs.getgo.backend.dtos.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedAdminDTO {
    private String email;
    private String name;
    private String surname;
    private String phoneNumber;
    private String address;
}
