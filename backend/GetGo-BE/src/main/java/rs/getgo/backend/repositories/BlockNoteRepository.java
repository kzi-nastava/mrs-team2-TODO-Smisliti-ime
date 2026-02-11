package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.BlockNote;
import rs.getgo.backend.model.entities.User;

import java.util.List;
import java.util.Optional;

public interface BlockNoteRepository extends JpaRepository<BlockNote, Long> {
    Optional<BlockNote> findByUserAndUnblockedAtIsNull(User user);
    List<BlockNote> findByUserOrderByBlockedAtDesc(User user);
}
