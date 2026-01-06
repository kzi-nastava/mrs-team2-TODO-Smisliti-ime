package rs.getgo.backend.dtos.inconsistencyReport;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetInconsistencyReportDTO {
    private Long id;
    private Long rideId;
    private Long passengerId;
    private String text;
}
