package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Report;

public interface RouteRepository extends JpaRepository<Report, Long> {
}
