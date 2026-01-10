package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.RideEstimate;

public interface RideEstimateRepository extends JpaRepository<RideEstimate, Long> {
    // no custom methods needed for now
}

