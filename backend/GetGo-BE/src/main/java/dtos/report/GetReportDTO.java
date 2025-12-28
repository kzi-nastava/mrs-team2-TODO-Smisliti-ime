package dtos.report;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetReportDTO {
    private String dateRange;
    private int totalRides;
    private double totalKm;
    private double averagePrice;
}