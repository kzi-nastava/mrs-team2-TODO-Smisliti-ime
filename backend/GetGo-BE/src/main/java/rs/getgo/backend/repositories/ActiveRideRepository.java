package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    Optional<ActiveRide> findFirstByDriverAndStatusOrderByScheduledTimeAsc(Driver driver, RideStatus status);
    @EntityGraph(attributePaths = {"route", "route.waypoints", "driver", "payingPassenger"})
    Optional<ActiveRide> findByPayingPassengerAndStatusIn(Passenger passenger, List<RideStatus> status);
    List<ActiveRide> findByStatusAndScheduledTimeLessThanEqual(RideStatus status, LocalDateTime time);
    boolean existsByPayingPassengerOrLinkedPassengersContaining(Passenger payingPassenger, Passenger linkedPassenger);
}
