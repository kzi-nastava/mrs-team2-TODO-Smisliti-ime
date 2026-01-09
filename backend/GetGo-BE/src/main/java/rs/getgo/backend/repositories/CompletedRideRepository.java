package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.CompletedRide;

public interface CompletedRideRepository extends JpaRepository<CompletedRide, Long> {
}
