package rs.getgo.backend.dtos.inconsistencyReport;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateInconsistencyReportDTO {
    @NotBlank(message = "Report text must not be empty")
    private String text;
}
