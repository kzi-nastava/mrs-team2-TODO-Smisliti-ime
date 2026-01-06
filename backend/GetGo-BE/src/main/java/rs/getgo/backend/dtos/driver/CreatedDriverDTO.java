package rs.getgo.backend.dtos.driver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedDriverDTO {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private Boolean activated;
}
