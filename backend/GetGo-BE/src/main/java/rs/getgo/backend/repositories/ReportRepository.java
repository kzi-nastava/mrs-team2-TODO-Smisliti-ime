package rs.getgo.backend.repositories;

import rs.getgo.backend.model.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}