package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.ActivationToken;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
}
