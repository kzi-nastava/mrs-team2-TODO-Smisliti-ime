package rs.getgo.backend.dtos.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AcceptDriverChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String status; // "APPROVED"
    private Long reviewedBy; // Admin ID who approved
    private LocalDateTime reviewedAt;
}