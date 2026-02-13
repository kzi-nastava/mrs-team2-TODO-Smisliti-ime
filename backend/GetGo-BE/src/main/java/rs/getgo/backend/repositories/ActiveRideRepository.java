package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.enums.RideStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ActiveRideRepository extends JpaRepository<ActiveRide, Long> {
    Optional<ActiveRide> findByDriverAndStatus(Driver driver, RideStatus status);
    List<ActiveRide> findByDriverAndStatusIn(Driver driver, List<RideStatus> statuses);
    List<ActiveRide> findByStatus(RideStatus status);
    @EntityGraph(attributePaths = {"route", "route.waypoints", "driver"})
    List<ActiveRide> findByStatusIn(List<RideStatus> statuses);
    boolean existsByDriverAndStatusIn(Driver driver, List<RideStatus> statuses);
    boolean existsByDriverAndStatus(Driver driver, RideStatus status);
    boolean existsByDriverAndStatusNot(Driver driver, RideStatus status);
    Optional<ActiveRide> findFirstByDriverAndStatusOrderByScheduledTimeAsc(Driver driver, RideStatus status);
    @EntityGraph(attributePaths = {"route", "route.waypoints", "driver", "payingPassenger"})
    List<ActiveRide> findByStatusAndScheduledTimeLessThanEqual(RideStatus status, LocalDateTime time);
    List<ActiveRide> findByStatusAndScheduledTimeBetween(RideStatus status, LocalDateTime from, LocalDateTime to);
    boolean existsByPayingPassengerAndStatusNot(Passenger passenger, RideStatus status);
    boolean existsByLinkedPassengersContainingAndStatusNot(Passenger passenger, RideStatus status);
    boolean existsByPayingPassengerAndStatusAndScheduledTimeBefore(Passenger passenger, RideStatus status, LocalDateTime time);
    boolean existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(Passenger passenger, RideStatus status, LocalDateTime time);

    @Query("""
        SELECT ar FROM ActiveRide ar
        LEFT JOIN ar.linkedPassengers lp
        WHERE (ar.payingPassenger = :passenger OR lp = :passenger)
          AND ar.status IN :statuses
    """)
    Optional<ActiveRide> findActiveRideForPassenger(@Param("passenger") Passenger passenger,
                                                    @Param("statuses") List<RideStatus> statuses);


}
