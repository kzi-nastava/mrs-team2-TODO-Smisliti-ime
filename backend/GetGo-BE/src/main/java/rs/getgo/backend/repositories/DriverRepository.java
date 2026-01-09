package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long> {
}
