package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
