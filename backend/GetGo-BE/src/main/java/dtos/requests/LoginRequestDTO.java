package dtos.requests;

import dtos.wayPoint.GetWayPointDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class LoginRequestDTO {
    String email;
    String password;
}
