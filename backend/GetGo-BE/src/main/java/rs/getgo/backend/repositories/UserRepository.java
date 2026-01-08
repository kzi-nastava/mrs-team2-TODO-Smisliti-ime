package rs.getgo.backend.repositories;

import rs.getgo.backend.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
