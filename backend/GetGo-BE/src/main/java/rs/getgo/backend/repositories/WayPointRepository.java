package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.WayPoint;

public interface WayPointRepository extends JpaRepository<WayPoint, Long> {
}
