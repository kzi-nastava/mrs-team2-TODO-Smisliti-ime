package rs.getgo.backend.repositories;

import rs.getgo.backend.model.entities.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<Ride, Long> {
}