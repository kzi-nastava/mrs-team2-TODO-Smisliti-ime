package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.FavoriteRide;

public interface FavoriteRideRepository extends JpaRepository<FavoriteRide, Long> {
}
