package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.getgo.backend.model.entities.InconsistencyReport;
import rs.getgo.backend.model.entities.Passenger;

import java.util.List;

public interface InconsistencyReportRepository extends JpaRepository<InconsistencyReport, Long> {
    List<InconsistencyReport> findByCompletedRide_IdOrderByCreatedAtDesc(Long rideId);

    @Query("SELECT r FROM InconsistencyReport r WHERE r.completedRide IS NULL AND r.passenger = :passenger")
    List<InconsistencyReport> findUnlinkedReportsByPassenger(@Param("passenger") Passenger passenger);
}
