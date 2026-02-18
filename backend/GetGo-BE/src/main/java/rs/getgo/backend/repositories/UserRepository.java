package rs.getgo.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rs.getgo.backend.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.getgo.backend.model.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    Page<User> findByIsBlocked(boolean isBlocked, Pageable pageable);
    Page<User> findByIsBlockedAndEmailContaining(boolean isBlocked, String email, Pageable pageable);

    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    Long findIdByEmail(@Param("email") String email);
}
