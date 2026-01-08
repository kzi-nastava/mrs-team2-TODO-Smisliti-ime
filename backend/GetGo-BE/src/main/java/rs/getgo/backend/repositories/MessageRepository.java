package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
