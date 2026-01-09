package rs.getgo.backend.dtos.user;

import lombok.*;

@Getter
@Setter
public class ForgotPasswordDTO {
    private String email;

    public ForgotPasswordDTO() {}

    public ForgotPasswordDTO(String email) {
        this.email = email;
    }
}

