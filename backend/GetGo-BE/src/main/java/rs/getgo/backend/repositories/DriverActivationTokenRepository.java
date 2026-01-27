package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.DriverActivationToken;

import java.util.Optional;

public interface DriverActivationTokenRepository extends JpaRepository<DriverActivationToken, Long> {
    Optional<DriverActivationToken> findByToken(String token);
}
