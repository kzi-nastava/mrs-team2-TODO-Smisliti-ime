package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Panic;

public interface PanicRepository extends JpaRepository<Panic, Long> {
}
