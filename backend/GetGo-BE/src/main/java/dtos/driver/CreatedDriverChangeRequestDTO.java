package dtos.driver;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedDriverChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String status;
    private String message;
}
