package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
