package rs.getgo.backend.dtos.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateDriverPasswordDTO {
    private String token;
    private String password;
    private String confirmPassword;
}
