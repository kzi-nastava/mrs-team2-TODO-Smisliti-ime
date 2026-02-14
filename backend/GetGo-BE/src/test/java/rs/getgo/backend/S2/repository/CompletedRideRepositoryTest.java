package rs.getgo.backend.S2.repository;

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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = {"/sql/S2/active-ride-s2.sql", "/sql/S2/completed-ride-s2.sql"})
public class CompletedRideRepositoryTest {

    @Autowired
    private CompletedRideRepository completedRideRepository;

    @Test
    public void fixture_shouldLoadCompletedRide() {
        Optional<CompletedRide> cr = completedRideRepository.findById(201L);
        assertThat(cr).isPresent();
        assertThat(cr.get().getEstimatedPrice()).isEqualTo(120.0);
    }
}

