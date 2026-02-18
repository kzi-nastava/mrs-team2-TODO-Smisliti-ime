package rs.getgo.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.getgo.backend.model.entities.CompletedRide;

import java.time.LocalDateTime;
import java.util.List;

public interface CompletedRideRepository extends JpaRepository<CompletedRide, Long> {
    Page<CompletedRide> findByDriverId(Long driverId, Pageable pageable);
    List<CompletedRide> findByDriverIdAndEndTimeAfter(Long driverId, LocalDateTime time);
    Page<CompletedRide> findByDriverIdAndStartTimeBetween(Long driverId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Find all rides where passenger is either paying or linked
    @Query("SELECT r FROM CompletedRide r WHERE r.payingPassengerId = :passengerId OR :passengerId MEMBER OF r.linkedPassengerIds")
    Page<CompletedRide> findByPassengerId(@Param("passengerId") Long passengerId, Pageable pageable);

    // Find rides ON a specific date (between startOfDay and endOfDay)
    @Query("SELECT r FROM CompletedRide r WHERE (r.payingPassengerId = :passengerId OR :passengerId MEMBER OF r.linkedPassengerIds) AND r.startTime >= :startOfDay AND r.startTime < :endOfDay")
    Page<CompletedRide> findByPassengerIdAndStartTimeBetween(
            @Param("passengerId") Long passengerId,
            @Param("startOfDay") java.time.LocalDateTime startOfDay,
            @Param("endOfDay") java.time.LocalDateTime endOfDay,
            Pageable pageable);

    List<CompletedRide> findByDriverEmailAndEndTimeBetween(String driverEmail, LocalDateTime start, LocalDateTime end);
    List<CompletedRide> findByPayingPassengerEmailAndEndTimeBetween(String email, LocalDateTime start, LocalDateTime end);
    List<CompletedRide> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM CompletedRide r JOIN r.linkedPassengerIds lpId " +
            "WHERE lpId IN (SELECT p.id FROM Passenger p WHERE p.email = :passengerEmail) " +
            "AND r.endTime BETWEEN :start AND :end")
    List<CompletedRide> findByLinkedPassengerEmailAndEndTimeBetween(
            @Param("passengerEmail") String passengerEmail,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
