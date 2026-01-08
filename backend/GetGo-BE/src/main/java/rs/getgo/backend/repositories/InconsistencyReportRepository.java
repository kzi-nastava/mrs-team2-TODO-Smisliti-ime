package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.InconsistencyReport;

public interface InconsistencyReportRepository extends JpaRepository<InconsistencyReport, Long> {
}
