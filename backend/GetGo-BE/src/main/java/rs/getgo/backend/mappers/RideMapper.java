package rs.getgo.backend.mappers;

import org.springframework.stereotype.Component;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.WayPoint;

@Component
public class RideMapper {
    public GetDriverActiveRideDTO buildDriverActiveRideDTO(ActiveRide ride) {
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
}
