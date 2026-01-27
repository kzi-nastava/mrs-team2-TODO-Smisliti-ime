package rs.getgo.backend.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
