package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.RidePrice;
import rs.getgo.backend.model.enums.VehicleType;

import java.util.Optional;

public interface RidePriceRepository extends JpaRepository<RidePrice, Long> {
    Optional<RidePrice> findByVehicleType(VehicleType vehicleType);
}
