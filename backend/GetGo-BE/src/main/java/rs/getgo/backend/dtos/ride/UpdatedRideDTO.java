package rs.getgo.backend.dtos.ride;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdatedRideDTO {
    private Long id;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
