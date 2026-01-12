package rs.getgo.backend.services;

import rs.getgo.backend.dtos.ride.GetRideDTO;

import java.time.LocalDate;
import java.util.List;

public interface DriverService {
    List<GetRideDTO> getDriverRides(Long driverId, LocalDate startDate);
}
