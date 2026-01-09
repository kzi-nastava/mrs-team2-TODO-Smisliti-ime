package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.RideCancellation;

public interface RideCancellationRepository extends JpaRepository<RideCancellation, Long> {
    // basic persistence only
}

