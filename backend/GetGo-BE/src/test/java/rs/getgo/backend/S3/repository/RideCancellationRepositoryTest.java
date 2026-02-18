package rs.getgo.backend.S3.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.GetGoBeApplication;
import rs.getgo.backend.model.entities.RideCancellation;
import rs.getgo.backend.repositories.RideCancellationRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sql/S3/ride-cancellation-test-data.sql")
public class RideCancellationRepositoryTest {

    @Autowired
    private RideCancellationRepository repo;

    @Test
    public void fixture_shouldLoadCancellation() {
        Optional<RideCancellation> rc = repo.findById(1L);
        assertTrue(rc.isPresent());
        assertEquals("test reason", rc.get().getReason());
    }

}
