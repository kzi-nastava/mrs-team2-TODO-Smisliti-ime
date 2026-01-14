package rs.getgo.backend.dtos.user;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateUserDTO {
    private String email;
    private String password;
    private String name;
    private String surname;
    private String phoneNumber;
    private String address;
}