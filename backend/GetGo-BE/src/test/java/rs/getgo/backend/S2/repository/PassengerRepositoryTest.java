package rs.getgo.backend.S2.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.GetGoBeApplication;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.repositories.PassengerRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = {"/sql/S2/active-ride-s2.sql"})
public class PassengerRepositoryTest {

    @Autowired
    private PassengerRepository passengerRepository;

    @Test
    public void fixture_shouldLoadS2Passenger() {
        Optional<Passenger> p = passengerRepository.findById(203L);
        assertThat(p).isPresent();
        assertThat(p.get().isCanAccessSystem()).isTrue();
    }
}
