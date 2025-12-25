package dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequestDTO {
    String email;
    String password;
    String name;
    String surname;
    String phone;

    public String getEmail() {
        return "";
    }
}
