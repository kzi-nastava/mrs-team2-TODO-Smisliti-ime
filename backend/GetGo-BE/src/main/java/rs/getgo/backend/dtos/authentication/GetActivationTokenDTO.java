package rs.getgo.backend.dtos.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetActivationTokenDTO {
    private Boolean valid;
    private String email; // If valid
    private String reason; // If invalid
}
