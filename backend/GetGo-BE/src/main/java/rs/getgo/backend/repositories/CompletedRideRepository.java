package rs.getgo.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.CompletedRide;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompletedRideRepository extends JpaRepository<CompletedRide, Long> {
    Page<CompletedRide> findByDriverId(Long driverId, Pageable pageable);
    List<CompletedRide> findByDriverIdAndEndTimeAfter(Long driverId, LocalDateTime last24Hours);
    Page<CompletedRide> findByDriverIdAndStartTimeBetween(Long driverId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<CompletedRide> findByPayingPassengerId(Long passengerId);
}
