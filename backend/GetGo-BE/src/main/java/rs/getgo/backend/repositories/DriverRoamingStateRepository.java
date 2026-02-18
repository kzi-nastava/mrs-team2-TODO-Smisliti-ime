package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.DriverRoamingState;
import rs.getgo.backend.model.enums.RideStatus;

import java.util.Optional;

public interface DriverRoamingStateRepository extends JpaRepository<DriverRoamingState, Long> {
    Optional<DriverRoamingState> findByDriver(Driver driver);
    void deleteByDriver(Driver driver);
}