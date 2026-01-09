package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.BlockNote;

public interface BlockNoteRepository extends JpaRepository<BlockNote, Long> {
}
