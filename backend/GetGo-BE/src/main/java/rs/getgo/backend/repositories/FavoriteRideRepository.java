package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.FavoriteRide;

import java.util.List;
import java.util.Optional;

public interface FavoriteRideRepository extends JpaRepository<FavoriteRide, Long> {
    List<FavoriteRide> findByUserEmail(String email);
    Optional<FavoriteRide> findByCompletedRideId(Long completedRideId);
    boolean existsByCompletedRideIdAndUserEmail(Long completedRideId, String userEmail);
}
