package rs.getgo.backend.services;

import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;

@Service
public interface DriverMatchingService {
    Driver findAvailableDriver(ActiveRide ride);
}