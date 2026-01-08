package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}
