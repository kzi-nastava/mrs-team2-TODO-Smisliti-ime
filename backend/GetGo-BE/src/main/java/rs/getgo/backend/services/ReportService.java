package rs.getgo.backend.services;

import rs.getgo.backend.dtos.report.ReportResponseDTO;

import java.time.LocalDate;

public interface ReportService {
    ReportResponseDTO getDriverReport(String driverEmail, LocalDate startDate, LocalDate endDate);
    ReportResponseDTO getPassengerReport(String passengerEmail, LocalDate startDate, LocalDate endDate);
    ReportResponseDTO getAdminDriversReport(LocalDate startDate, LocalDate endDate);
    ReportResponseDTO getAdminPassengersReport(LocalDate startDate, LocalDate endDate);
    ReportResponseDTO getAdminDriverReport(String email, LocalDate startDate, LocalDate endDate);
    ReportResponseDTO getAdminPassengerReport(String email, LocalDate startDate, LocalDate endDate);
}
