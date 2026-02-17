package rs.getgo.backend.services.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import rs.getgo.backend.dtos.report.DailyReportDTO;
import rs.getgo.backend.dtos.report.ReportResponseDTO;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.model.enums.UserRole;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.services.ReportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    private final CompletedRideRepository completedRideRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(CompletedRideRepository completedRideRepository,
                             UserRepository userRepository) {
        this.completedRideRepository = completedRideRepository;
        this.userRepository = userRepository;
    }

    public ReportResponseDTO getDriverReport(String driverEmail, LocalDate startDate, LocalDate endDate) {
        validateUser(driverEmail, UserRole.DRIVER);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<CompletedRide> rides = completedRideRepository
                .findByDriverEmailAndEndTimeBetween(driverEmail, start, end);

        return buildDriverReport(rides, startDate, endDate);
    }

    public ReportResponseDTO getPassengerReport(String passengerEmail, LocalDate startDate, LocalDate endDate) {
        validateUser(passengerEmail, UserRole.PASSENGER);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<CompletedRide> payingRides = completedRideRepository
                .findByPayingPassengerEmailAndEndTimeBetween(passengerEmail, start, end);

        List<CompletedRide> linkedRides = completedRideRepository
                .findByLinkedPassengerEmailAndEndTimeBetween(passengerEmail, start, end);

        return buildPassengerReport(payingRides, linkedRides, startDate, endDate);
    }

    public ReportResponseDTO getAdminDriversReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<CompletedRide> rides = completedRideRepository.findByEndTimeBetween(start, end);

        return buildDriverReport(rides, startDate, endDate);
    }

    public ReportResponseDTO getAdminPassengersReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<CompletedRide> rides = completedRideRepository.findByEndTimeBetween(start, end);

        return buildPassengerReport(rides, List.of(), startDate, endDate);
    }

    public ReportResponseDTO getAdminDriverReport(String email, LocalDate startDate, LocalDate endDate) {
        return getDriverReport(email, startDate, endDate);
    }

    public ReportResponseDTO getAdminPassengerReport(String email, LocalDate startDate, LocalDate endDate) {
        return getPassengerReport(email, startDate, endDate);
    }

    private ReportResponseDTO buildDriverReport(List<CompletedRide> rides, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, DailyReportDTO> dailyMap = initDailyMap(startDate, endDate);

        for (CompletedRide ride : rides) {
            LocalDate day = ride.getEndTime().toLocalDate();
            DailyReportDTO daily = dailyMap.get(day);
            if (daily != null) {
                daily.setNumberOfRides(daily.getNumberOfRides() + 1);
                daily.setTotalKilometers(daily.getTotalKilometers() + ride.getEstDistanceKm());
                daily.setTotalMoney(daily.getTotalMoney() + ride.getEstimatedPrice());
            }
        }

        return assembleResponse(dailyMap, startDate, endDate);
    }

    private ReportResponseDTO buildPassengerReport(List<CompletedRide> payingRides,
                                                   List<CompletedRide> linkedRides,
                                                   LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, DailyReportDTO> dailyMap = initDailyMap(startDate, endDate);

        for (CompletedRide ride : payingRides) {
            LocalDate day = ride.getEndTime().toLocalDate();
            DailyReportDTO daily = dailyMap.get(day);
            if (daily != null) {
                daily.setNumberOfRides(daily.getNumberOfRides() + 1);
                daily.setTotalKilometers(daily.getTotalKilometers() + ride.getEstDistanceKm());
                daily.setTotalMoney(daily.getTotalMoney() + ride.getEstimatedPrice());
            }
        }

        for (CompletedRide ride : linkedRides) {
            LocalDate day = ride.getEndTime().toLocalDate();
            DailyReportDTO daily = dailyMap.get(day);
            if (daily != null) {
                daily.setNumberOfRides(daily.getNumberOfRides() + 1);
                daily.setTotalKilometers(daily.getTotalKilometers() + ride.getEstDistanceKm());
            }
        }

        return assembleResponse(dailyMap, startDate, endDate);
    }

    private Map<LocalDate, DailyReportDTO> initDailyMap(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, DailyReportDTO> map = new LinkedHashMap<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            map.put(current, new DailyReportDTO(current, 0, 0.0, 0.0));
            current = current.plusDays(1);
        }
        return map;
    }

    private ReportResponseDTO assembleResponse(Map<LocalDate, DailyReportDTO> dailyMap,
                                               LocalDate startDate, LocalDate endDate) {
        List<DailyReportDTO> dailyStats = new ArrayList<>(dailyMap.values());

        int totalRides = dailyStats.stream().mapToInt(DailyReportDTO::getNumberOfRides).sum();
        double totalKm = dailyStats.stream().mapToDouble(DailyReportDTO::getTotalKilometers).sum();
        double totalMoney = dailyStats.stream().mapToDouble(DailyReportDTO::getTotalMoney).sum();

        long numberOfDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        totalKm = Math.round(totalKm * 100.0) / 100.0;
        totalMoney = Math.round(totalMoney * 100.0) / 100.0;

        double avgRides = Math.round((double) totalRides / numberOfDays * 100.0) / 100.0;
        double avgKm = Math.round(totalKm / numberOfDays * 100.0) / 100.0;
        double avgMoney = Math.round(totalMoney / numberOfDays * 100.0) / 100.0;

        return new ReportResponseDTO(dailyStats, totalRides, totalKm, totalMoney, avgRides, avgKm, avgMoney);
    }

    private void validateUser(String email, UserRole expectedRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        expectedRole.name() + " with email " + email + " not found"
                ));

        if (user.getRole() != expectedRole) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User with email " + email + " is not a " + expectedRole.name().toLowerCase()
            );
        }
    }
}