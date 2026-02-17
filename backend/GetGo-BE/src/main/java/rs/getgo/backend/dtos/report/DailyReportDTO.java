package rs.getgo.backend.dtos.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DailyReportDTO {
    private LocalDate date;
    private int numberOfRides;
    private double totalKilometers;
    private double totalMoney;
}