package rs.getgo.backend.repositories;

import rs.getgo.backend.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // find user by email for login/register checks
    Optional<User> findByEmail(String email);
}
