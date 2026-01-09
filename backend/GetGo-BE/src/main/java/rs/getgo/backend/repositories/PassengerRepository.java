package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Passenger;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
}
