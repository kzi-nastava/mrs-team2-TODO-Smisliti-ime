package dtos.ride;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UpdatedRideDTO {
    private Long id;
    private String status;
    private LocalDateTime finishedTime;
}
