package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.CompletedRide;

import java.time.LocalDateTime;
import java.util.List;

public interface CompletedRideRepository extends JpaRepository<CompletedRide, Long> {
    List<CompletedRide> findByDriverId(Long driverId);
    List<CompletedRide> findByDriverIdAndEndTimeAfter(Long driverId, LocalDateTime last24Hours);
}
