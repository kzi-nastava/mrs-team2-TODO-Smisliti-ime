package rs.getgo.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.GetGoBeApplication;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.repositories.CompletedRideRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sql/completed-ride-test-data.sql")
public class CompletedRideRepositoryTest {

    @Autowired
    private CompletedRideRepository completedRideRepository;

    @Test
    public void fixture_shouldLoadCompletedRide() {
        Optional<CompletedRide> cr = completedRideRepository.findById(11L);
        assertTrue(cr.isPresent());
        assertEquals(150.0, cr.get().getEstimatedPrice());
    }

}
