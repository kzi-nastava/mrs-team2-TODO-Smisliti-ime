package rs.getgo.backend.repository;

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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sql/panic-test-data.sql")
public class PanicRepositoryTest {

    @Autowired
    private PanicRepository panicRepository;

    @Test
    public void fixture_shouldLoadPanicRecord() {
        List<Panic> all = panicRepository.findAll();
        assertFalse(all.isEmpty(), "Fixture should load at least one panic");
        boolean hasId1 = all.stream().anyMatch(p -> p.getId() != null && p.getId() == 1L);
        assertTrue(hasId1, "Fixture must contain panic with id=1");
    }

}
