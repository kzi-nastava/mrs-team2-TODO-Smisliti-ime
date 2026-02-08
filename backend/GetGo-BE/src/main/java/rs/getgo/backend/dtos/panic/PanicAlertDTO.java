package rs.getgo.backend.dtos.panic;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class PanicAlertDTO {
    private Long panicId;
    private Long rideId;
    private Long triggeredByUserId;
    private LocalDateTime triggeredAt;
    private Boolean status; // e.g. "PANIC"
}

