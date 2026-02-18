package rs.getgo.backend.S3.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.repositories.CompletedRideRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = rs.getgo.backend.GetGoBeApplication.class)
@Sql("/sql/S3/completed-ride-test-data.sql")
@TestPropertySource(locations = "classpath:application-test.properties")
public class CompletedRideRepositoryTest {

    @Autowired
    private CompletedRideRepository completedRideRepository;

    @Test
    void shouldReturnAllDriverRides_WhenAllEndTimesAreAfterGivenTime() {
        List<CompletedRide> result = completedRideRepository.findByDriverIdAndEndTimeAfter(
                1L, LocalDateTime.of(2025, 1, 1, 0, 0));
        assertEquals(3, result.size());
    }

    @Test
    void shouldReturnOnlyLaterRide_WhenCutoffIsBetweenEndTimes() {
        List<CompletedRide> result = completedRideRepository.findByDriverIdAndEndTimeAfter(
                1L, LocalDateTime.of(2025, 4, 1, 0, 0));
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getId().equals(2L)));
    }

    @Test
    void shouldReturnEmptyList_WhenAllEndTimesAreBeforeGivenTime() {
        List<CompletedRide> result = completedRideRepository.findByDriverIdAndEndTimeAfter(
                1L, LocalDateTime.of(2027, 1, 1, 0, 0));
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyList_WhenDriverHasNoCompletedRides() {
        List<CompletedRide> result = completedRideRepository.findByDriverIdAndEndTimeAfter(
                999L, LocalDateTime.of(2024, 1, 1, 0, 0));
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotReturnOtherDriversRides() {
        List<CompletedRide> result = completedRideRepository.findByDriverIdAndEndTimeAfter(
                2L, LocalDateTime.of(2025, 1, 1, 0, 0));
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());
    }

    @Test
    void shouldExcludeRide_WhenEndTimeEqualsGivenTime() {
        List<CompletedRide> result = completedRideRepository.findByDriverIdAndEndTimeAfter(
                1L, LocalDateTime.of(2025, 6, 1, 14, 45, 0));
        assertEquals(1, result.size());
        assertEquals(11L, result.get(0).getId());
    }

    @Test
    public void fixture_shouldLoadCompletedRide() {
        Optional<CompletedRide> cr = completedRideRepository.findById(11L);
        assertTrue(cr.isPresent());
        assertEquals(150.0, cr.get().getEstimatedPrice());
    }
}