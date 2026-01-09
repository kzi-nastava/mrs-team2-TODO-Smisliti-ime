package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.ActiveRide;

public interface ActiveRideRepository extends JpaRepository<ActiveRide, Long> {
}
