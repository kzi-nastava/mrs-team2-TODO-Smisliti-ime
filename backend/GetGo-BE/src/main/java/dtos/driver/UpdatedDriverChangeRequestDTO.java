package dtos.driver;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdatedDriverChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String status; // Request status
    private String rejectionReason;
    private Long reviewedBy; // Admin id that reviewed request
    private LocalDateTime reviewedAt;
}
