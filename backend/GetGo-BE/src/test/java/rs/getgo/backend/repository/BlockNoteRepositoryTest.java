package rs.getgo.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.model.entities.BlockNote;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.repositories.BlockNoteRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = rs.getgo.backend.GetGoBeApplication.class)
@Sql("/sql/block-note-test-data.sql")
@TestPropertySource(locations = "classpath:application-test.properties")
public class BlockNoteRepositoryTest {

    @Autowired
    private BlockNoteRepository blockNoteRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    @Test
    void shouldReturnBlockNote_WhenUserHasActiveBlock() {
        User blockedUser = entityManager.find(User.class, 200L);
        Optional<BlockNote> result = blockNoteRepository.findByUserAndUnblockedAtIsNull(blockedUser);
        assertTrue(result.isPresent());
        assertEquals("Misbehavior", result.get().getReason());
        assertNull(result.get().getUnblockedAt());
    }

    @Test
    void shouldReturnEmpty_WhenUserHasNoBlocks() {
        User freeUser = entityManager.find(User.class, 201L);
        Optional<BlockNote> result = blockNoteRepository.findByUserAndUnblockedAtIsNull(freeUser);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmpty_WhenUserHasOnlyExpiredBlocks() {
        User previouslyBlockedUser = entityManager.find(User.class, 202L);
        Optional<BlockNote> result = blockNoteRepository.findByUserAndUnblockedAtIsNull(previouslyBlockedUser);
        assertTrue(result.isEmpty());
    }
}