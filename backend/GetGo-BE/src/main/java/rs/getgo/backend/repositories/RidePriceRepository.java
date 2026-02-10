package rs.getgo.backend.repositories;

import rs.getgo.backend.model.entities.RidePrice;
import rs.getgo.backend.model.enums.VehicleType;

import java.util.Optional;

public interface RidePriceRepository {
    Optional<RidePrice> findByVehicleType(VehicleType vehicleType);
}
