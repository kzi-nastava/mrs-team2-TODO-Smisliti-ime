package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.InconsistencyReport;

import java.util.List;

public interface InconsistencyReportRepository extends JpaRepository<InconsistencyReport, Long> {
    List<InconsistencyReport> findByCompletedRide_IdOrderByCreatedAtDesc(Long rideId);
}
