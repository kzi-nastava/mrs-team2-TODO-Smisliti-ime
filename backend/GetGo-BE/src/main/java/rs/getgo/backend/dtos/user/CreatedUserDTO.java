package rs.getgo.backend.dtos.user;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedUserDTO {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String address;
    private String phoneNumber;
    private Boolean blocked;
}