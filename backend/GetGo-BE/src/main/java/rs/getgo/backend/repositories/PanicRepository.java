package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Panic;

import java.util.List;
import java.util.Optional;

public interface PanicRepository extends JpaRepository<Panic, Long> {
    public List<Optional<Panic>> findByRideId(Long rideId);
}
