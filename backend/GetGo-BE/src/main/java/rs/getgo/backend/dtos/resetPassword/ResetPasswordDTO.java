package rs.getgo.backend.dtos.resetPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordDTO {

    /*@NotBlank(message = "Token is required")*/
    private String token;

    /*@NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")*/
    private String password;
}