package rs.getgo.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.GetGoBeApplication;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.repositories.DriverRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sql/driver-test-data.sql")
public class DriverRepositoryTest {

    @Autowired
    private DriverRepository driverRepository;

    @Test
    public void fixture_shouldContainDrivers() {
        var found = driverRepository.findByEmail("dtest@test.com");
        assertTrue(found.isPresent());
        Driver d = found.get();
        assertTrue(Boolean.TRUE.equals(d.getActive()));
    }

    @Test
    public void fixture_findByIsActive_returnsOnlyActive() {
        List<Driver> actives = driverRepository.findByIsActive(true);
        assertTrue(actives.stream().anyMatch(d -> d.getEmail().equals("dtest@test.com")));
        assertTrue(actives.stream().noneMatch(d -> d.getEmail().equals("dtest2@test.com")));
    }

}
