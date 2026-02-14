package rs.getgo.backend.S2.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.GetGoBeApplication;
import rs.getgo.backend.model.entities.Panic;
import rs.getgo.backend.repositories.PanicRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = {"/sql/S2/active-ride-s2.sql", "/sql/S2/panic-s2.sql"})
public class PanicRepositoryTest {

    @Autowired
    private PanicRepository panicRepository;

    @Test
    public void fixture_shouldLoadPanic() {
        List<Panic> panics = panicRepository.findAll();
        assertThat(panics).isNotEmpty();
        assertThat(panics.get(0).getRideId()).isEqualTo(101L);
    }

    @Test
    public void findByRideId_returnsOptionalList() {
        List<java.util.Optional<Panic>> result = panicRepository.findByRideId(101L);
        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).isPresent();
        assertThat(result.get(0).get().getId()).isEqualTo(51L);
    }
}

