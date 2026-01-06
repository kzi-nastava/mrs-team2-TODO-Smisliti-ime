package rs.getgo.backend.dtos.login;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedLoginDTO {
    private Long id;
    private String role;
    private String token;
}