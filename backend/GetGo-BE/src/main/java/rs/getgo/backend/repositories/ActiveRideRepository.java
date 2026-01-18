package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Panic;

import java.util.List;
import java.util.Optional;

public interface ActiveRideRepository extends JpaRepository<ActiveRide, Long> {
}
