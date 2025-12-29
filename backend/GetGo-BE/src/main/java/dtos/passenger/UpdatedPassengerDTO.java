package dtos.passenger;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdatedPassengerDTO {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String phone;
    private String address;
    private String profilePictureUrl;
}
