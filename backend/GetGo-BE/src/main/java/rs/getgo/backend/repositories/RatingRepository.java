package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Rating;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByCompletedRide_Id(Long rideId);
    boolean existsByPassenger_IdAndCompletedRide_Id(Long passengerId, Long rideId);
    List<Rating> findByCompletedRide_DriverIdOrderByIdDesc(Long driverId);
}
