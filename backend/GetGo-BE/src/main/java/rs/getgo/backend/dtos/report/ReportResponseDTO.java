package rs.getgo.backend.dtos.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ReportResponseDTO {
    private List<DailyReportDTO> dailyStats;

    private int totalRides;
    private double totalKilometers;
    private double totalMoney;

    private double avgRidesPerDay;
    private double avgKilometersPerDay;
    private double avgMoneyPerDay;
}