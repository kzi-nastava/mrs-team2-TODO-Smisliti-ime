package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.GlobalReport;

public interface GlobalReportRepository  extends JpaRepository<GlobalReport, Long> {
}
