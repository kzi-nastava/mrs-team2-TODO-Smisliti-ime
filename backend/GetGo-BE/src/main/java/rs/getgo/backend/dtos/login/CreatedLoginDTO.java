package rs.getgo.backend.dtos.login;

import lombok.*;
import rs.getgo.backend.model.enums.UserRole;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedLoginDTO {
    private Long id;
    private String role;
    private String token;

    public CreatedLoginDTO(Long id, UserRole role, String token) {
        this.id = id;
        this.role = role.name();
        this.token = token;
    }
}