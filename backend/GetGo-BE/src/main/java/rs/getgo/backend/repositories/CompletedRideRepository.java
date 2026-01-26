package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.CompletedRide;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompletedRideRepository extends JpaRepository<CompletedRide, Long> {
    List<CompletedRide> findByDriverId(Long driverId);
    List<CompletedRide> findByDriverIdAndEndTimeAfter(Long driverId, LocalDateTime last24Hours);
}
