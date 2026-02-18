package rs.getgo.backend.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.dtos.report.ReportResponseDTO;
import rs.getgo.backend.services.ReportService;
import rs.getgo.backend.utils.AuthUtils;
import java.time.LocalDate;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping(value = "/driver", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportResponseDTO> getDriverReport(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {

        String email = AuthUtils.getCurrentUserEmail();
        ReportResponseDTO report = reportService.getDriverReport(email, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @PreAuthorize("hasRole('PASSENGER')")
    @GetMapping(value = "/passenger", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportResponseDTO> getPassengerReport(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {

        String email = AuthUtils.getCurrentUserEmail();
        ReportResponseDTO report = reportService.getPassengerReport(email, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/drivers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportResponseDTO> getAdminDriversReport(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {

        ReportResponseDTO report = reportService.getAdminDriversReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/passengers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportResponseDTO> getAdminPassengersReport(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {

        ReportResponseDTO report = reportService.getAdminPassengersReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/driver", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportResponseDTO> getAdminDriverReport(
            @RequestParam String email,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {

        ReportResponseDTO report = reportService.getAdminDriverReport(email, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/passenger", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportResponseDTO> getAdminPassengerReport(
            @RequestParam String email,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {

        ReportResponseDTO report = reportService.getAdminPassengerReport(email, startDate, endDate);
        return ResponseEntity.ok(report);
    }
}