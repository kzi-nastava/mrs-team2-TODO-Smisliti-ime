package dtos.inconsistencyReport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class CreateInconsistencyReportDTO {
    private Long rideId;
    private Long passengerId;
    private String text;
}
