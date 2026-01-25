package rs.getgo.backend.services.impl.rides;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import rs.getgo.backend.dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import rs.getgo.backend.dtos.ride.GetPassengerActiveRideDTO;
import rs.getgo.backend.dtos.ride.GetRideTrackingDTO;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.InconsistencyReportRepository;
import rs.getgo.backend.repositories.PassengerRepository;

import java.util.List;

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

    public GetPassengerActiveRideDTO getPassengerActiveRide(String passengerEmail) {
        Passenger passenger = passengerRepository.findByEmail(passengerEmail)
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

        ActiveRide ride = activeRideRepository.findByPayingPassengerAndStatusIn(
                passenger,
                List.of(
                        RideStatus.SCHEDULED,
                        RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE,
                        RideStatus.DRIVER_READY,
                        RideStatus.DRIVER_INCOMING,
                        RideStatus.DRIVER_ARRIVED,
                        RideStatus.ACTIVE
                )
        ).stream().findFirst().orElse(null);

        if (ride == null) return null;

        return buildGetPassengerActiveRideDTO(ride);
    }

    public GetPassengerActiveRideDTO buildGetPassengerActiveRideDTO(ActiveRide ride) {
        GetPassengerActiveRideDTO dto = new GetPassengerActiveRideDTO();
        dto.setRideId(ride.getId());
        dto.setStartingPoint(ride.getRoute().getStartingPoint());
        dto.setEndingPoint(ride.getRoute().getEndingPoint());
        dto.setEstimatedPrice(ride.getEstimatedPrice());
        dto.setEstimatedTimeMin(ride.getRoute().getEstTimeMin());
        dto.setStatus(ride.getStatus().toString());

        if (ride.getDriver() != null) {
            dto.setDriverName(ride.getDriver().getName() + " " + ride.getDriver().getSurname());
        }

        // Add waypoints
        dto.setLatitudes(ride.getRoute().getWaypoints().stream()
                .map(WayPoint::getLatitude)
                .toList());
        dto.setLongitudes(ride.getRoute().getWaypoints().stream()
                .map(WayPoint::getLongitude)
                .toList());
        dto.setAddresses(ride.getRoute().getWaypoints().stream()
                .map(WayPoint::getAddress)
                .toList());

        return dto;
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
        Passenger passenger = passengerRepository.findById(1L)
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
