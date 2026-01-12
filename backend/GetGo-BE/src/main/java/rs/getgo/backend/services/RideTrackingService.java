package rs.getgo.backend.services;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import rs.getgo.backend.dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import rs.getgo.backend.dtos.ride.GetRideTrackingDTO;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.repositories.InconsistencyReportRepository;
import rs.getgo.backend.repositories.PassengerRepository;

@Service
public class RideTrackingService {

    private final ActiveRideRepository activeRideRepository;
    private final InconsistencyReportRepository reportRepository;
    private final PassengerRepository passengerRepository;

    public RideTrackingService(ActiveRideRepository activeRideRepository, InconsistencyReportRepository reportRepository, PassengerRepository passengerRepository) {
        this.activeRideRepository = activeRideRepository;
        this.reportRepository = reportRepository;
        this.passengerRepository = passengerRepository;
    }

    public GetRideTrackingDTO getRideTracking(Long rideId) {
        ActiveRide ride = activeRideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        WayPoint currentLocation = ride.getCurrentLocation();
        Route route = ride.getRoute();

        // Calculate estimated time remaining in minutes
        double estimatedTimeMinutes = calculateEstimatedTime(currentLocation, route, ride.getVehicleType());

        return new GetRideTrackingDTO(
                ride.getId(),
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                route.getStartingPoint(),
                route.getEndingPoint(),
                estimatedTimeMinutes
        );
    }

    private double calculateEstimatedTime(WayPoint current, Route route, VehicleType vehicleType) {
        // For now, simplified estimate using route's estimated time
        // In the future, will be replaced with API call to Mapbox/OSRM
        return route.getEstTimeMin();
    }

    public CreatedInconsistencyReportDTO saveInconsistencyReport(long rideId, CreateInconsistencyReportDTO dto) throws Exception {
        ActiveRide activeRide = activeRideRepository.findById(rideId).orElseThrow(() -> new Exception("Active ride not found"));

        InconsistencyReport report = new InconsistencyReport();
        report.setText(dto.getText());

        // Will be set later when completed ride is implemented
//        Passenger loggedInPassenger = securityService.getLoggedInPassenger();
        Passenger passenger = passengerRepository.findById(10L)
                .orElseThrow(() -> new Exception("Passenger not found"));
//        report.setPassenger(loggedInPassenger);
        report.setPassenger(passenger);

        report.setCompletedRide(null); // ride is not over yet

        InconsistencyReport saved = reportRepository.save(report);

        return new CreatedInconsistencyReportDTO(
                saved.getId(),
                activeRide.getId(),
                saved.getPassenger() != null ? saved.getPassenger().getId() : null,
                saved.getText()
        );
    }


}
