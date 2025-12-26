package dtos.inconsistencyReport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class CreatedInconsistencyReportDTO {
    private Long id;
    private Long rideId;
    private Long passengerId;
    private String text;
}
