package rs.getgo.backend.dtos.login;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateLoginDTO {
    private String email;
    private String password;
}
