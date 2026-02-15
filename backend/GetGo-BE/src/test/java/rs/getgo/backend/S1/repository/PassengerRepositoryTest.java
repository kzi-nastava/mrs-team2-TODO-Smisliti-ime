package rs.getgo.backend.S1.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.repositories.PassengerRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = rs.getgo.backend.GetGoBeApplication.class)
@Sql("/sql/S1/passenger-test-data.sql")
@TestPropertySource(locations = "classpath:application-test.properties")
public class PassengerRepositoryTest {

    @Autowired
    private PassengerRepository passengerRepository;

    @Test
    void shouldReturnPassenger_WhenEmailExists() {
        Optional<Passenger> result = passengerRepository.findByEmail("findme@test.com");
        assertTrue(result.isPresent());
        assertEquals("Find", result.get().getName());
    }

    @Test
    void shouldReturnEmpty_WhenEmailDoesNotExist() {
        Optional<Passenger> result = passengerRepository.findByEmail("nonexistent@test.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmpty_WhenEmailCaseDoesNotMatch() {
        Optional<Passenger> result = passengerRepository.findByEmail("FINDME@TEST.COM");
        assertTrue(result.isEmpty());
    }
}