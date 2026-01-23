package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.enums.RideStatus;

import java.util.List;
import java.util.Optional;

public interface ActiveRideRepository extends JpaRepository<ActiveRide, Long> {
    Optional<ActiveRide> findByDriverAndStatus(Driver driver, RideStatus status);
    List<ActiveRide> findByDriverAndStatusIn(Driver driver, List<RideStatus> statuses);
    @EntityGraph(attributePaths = {"route", "route.waypoints", "driver"})
    List<ActiveRide> findByStatusIn(List<RideStatus> statuses);
    boolean existsByDriverAndStatusIn(Driver driver, List<RideStatus> statuses);
    boolean existsByDriverAndStatus(Driver driver, RideStatus status);
}
