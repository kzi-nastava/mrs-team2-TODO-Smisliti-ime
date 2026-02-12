package rs.getgo.backend.mappers;

import org.springframework.stereotype.Component;
import rs.getgo.backend.dtos.ride.GetActiveRideDTO;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.WayPoint;

import java.util.List;

@Component
public class RideMapper {
    public GetDriverActiveRideDTO toDriverActiveRideDTO(ActiveRide ride) {
        GetDriverActiveRideDTO dto = new GetDriverActiveRideDTO();
        dto.setRideId(ride.getId());
        dto.setStartingPoint(ride.getRoute().getStartingPoint());
        dto.setEndingPoint(ride.getRoute().getEndingPoint());
        dto.setEstimatedPrice(ride.getEstimatedPrice());
        dto.setEstimatedTimeMin(ride.getRoute().getEstTimeMin());
        dto.setPassengerName(ride.getPayingPassenger().getName() + " " + ride.getPayingPassenger().getSurname());
        dto.setPassengerCount(1 + (ride.getLinkedPassengers() != null ? ride.getLinkedPassengers().size() : 0));
        dto.setStatus(ride.getStatus().toString());
        dto.setScheduledTime(ride.getScheduledTime());

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

    public GetActiveRideDTO toGetActiveRideDTO(ActiveRide ride) {
        GetActiveRideDTO dto = new GetActiveRideDTO();

        dto.setId(ride.getId());
        dto.setStartingPoint(ride.getRoute().getStartingPoint());
        dto.setEndingPoint(ride.getRoute().getEndingPoint());
        dto.setWaypointAddresses(
                ride.getRoute().getWaypoints().stream()
                        .map(WayPoint::getAddress)
                        .toList()
        );

        // Driver info, may be null for scheduled rides
        Driver driver = ride.getDriver();
        if (driver != null) {
            dto.setDriverEmail(driver.getEmail());
            dto.setDriverName(driver.getName() + " " + driver.getSurname());
        }

        // Passenger info
        dto.setPayingPassengerEmail(ride.getPayingPassenger().getEmail());
        dto.setLinkedPassengerEmails(
                ride.getLinkedPassengers() != null
                        ? ride.getLinkedPassengers().stream()
                        .map(rs.getgo.backend.model.entities.Passenger::getEmail)
                        .toList()
                        : List.of()
        );

        // Ride details
        dto.setEstimatedPrice(ride.getEstimatedPrice());
        dto.setSetEstimatedDurationMin(ride.getEstimatedDurationMin());
        dto.setScheduledTime(ride.getScheduledTime());
        dto.setActualStartTime(ride.getActualStartTime());
        dto.setStatus(ride.getStatus().toString());
        dto.setVehicleType(ride.getVehicleType() != null ? ride.getVehicleType().toString() : "ANY");
        dto.setNeedsBabySeats(ride.isNeedsBabySeats());
        dto.setNeedsPetFriendly(ride.isNeedsPetFriendly());

        return dto;
    }
}
