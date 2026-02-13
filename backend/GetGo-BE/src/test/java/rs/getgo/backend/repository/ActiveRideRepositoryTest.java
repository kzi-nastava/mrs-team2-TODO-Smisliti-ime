package rs.getgo.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.repositories.PassengerRepository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = rs.getgo.backend.GetGoBeApplication.class)
@Sql("/sql/active-ride-test-data.sql")
@TestPropertySource(locations = "classpath:application-test.properties")
public class ActiveRideRepositoryTest {

    @Autowired
    private ActiveRideRepository activeRideRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PassengerRepository passengerRepository;

//    @Autowired
//    private JdbcTemplate jdbcTemplate;

    private Driver driver1;
    private Driver driver2;
    private Passenger passenger3;
    private Passenger passenger4;
    private Passenger passenger5;

    @BeforeEach
    void setUp() {
        driver1 = driverRepository.findById(1L).orElseThrow();
        driver2 = driverRepository.findById(2L).orElseThrow();
        passenger3 = passengerRepository.findById(3L).orElseThrow();
        passenger4 = passengerRepository.findById(4L).orElseThrow();
        passenger5 = passengerRepository.findById(5L).orElseThrow();
    }

    // existsByDriverAndStatusIn

    @Test
    void shouldReturnTrue_WhenDriverHasRideMatchingAnyStatus() {
        assertTrue(activeRideRepository.existsByDriverAndStatusIn(
                driver1, List.of(RideStatus.ACTIVE, RideStatus.CANCELLED)));
    }

    @Test
    void shouldReturnFalse_WhenDriverHasNoRideMatchingAnyStatus() {
        assertFalse(activeRideRepository.existsByDriverAndStatusIn(
                driver1, List.of(RideStatus.CANCELLED, RideStatus.STOPPED)));
    }

    @Test
    void shouldReturnFalse_WhenStatusListIsEmpty() {
        assertFalse(activeRideRepository.existsByDriverAndStatusIn(driver1, List.of()));
    }

    @Test
    void shouldReturnFalse_WhenDriverHasNoRides() {
        assertFalse(activeRideRepository.existsByDriverAndStatusIn(
                driver2, List.of(RideStatus.ACTIVE, RideStatus.SCHEDULED)));
    }

    // existsByDriverAndStatus

    @Test
    void shouldReturnTrue_WhenDriverHasRideWithStatus() {
        assertTrue(activeRideRepository.existsByDriverAndStatus(driver1, RideStatus.ACTIVE));
    }

    @Test
    void shouldReturnFalse_WhenDriverHasNoRideWithStatus() {
        assertFalse(activeRideRepository.existsByDriverAndStatus(driver2, RideStatus.ACTIVE));
    }

    // findByDriverAndStatus

    @Test
    void shouldReturnRide_WhenDriverAndActiveStatusMatch() {
        Optional<ActiveRide> result = activeRideRepository.findByDriverAndStatus(driver1, RideStatus.ACTIVE);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void shouldReturnRide_WhenDriverAndScheduledStatusMatch() {
        Optional<ActiveRide> result = activeRideRepository.findByDriverAndStatus(driver1, RideStatus.SCHEDULED);
        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getId());
    }

    @Test
    void shouldReturnEmpty_WhenDriverHasNoRides() {
        Optional<ActiveRide> result = activeRideRepository.findByDriverAndStatus(driver2, RideStatus.ACTIVE);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmpty_WhenStatusDoesNotMatch() {
        Optional<ActiveRide> result = activeRideRepository.findByDriverAndStatus(driver1, RideStatus.CANCELLED);
        assertTrue(result.isEmpty());
    }

    // findByStatus

    @Test
    void shouldReturnOneRide_WhenStatusIsActive() {
        List<ActiveRide> result = activeRideRepository.findByStatus(RideStatus.ACTIVE);
        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnTwoRides_WhenStatusIsScheduled() {
        List<ActiveRide> result = activeRideRepository.findByStatus(RideStatus.SCHEDULED);
        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyList_WhenNoRidesMatchStatus() {
        assertTrue(activeRideRepository.findByStatus(RideStatus.CANCELLED).isEmpty());
    }

    // findByStatusAndScheduledTimeLessThanEqual

    @Test
    void shouldReturnBothRides_WhenCutoffIsAfterAllScheduledTimes() {
        List<ActiveRide> result = activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                RideStatus.SCHEDULED,
                LocalDateTime.of(2030, 1, 1, 10, 0, 1));
        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnOnlyEarlierRide_WhenCutoffIsBetweenScheduledTimes() {
        List<ActiveRide> result = activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                RideStatus.SCHEDULED,
                LocalDateTime.of(2026, 1, 1, 0, 0));
        assertEquals(1, result.size());
        assertEquals(3L, result.getFirst().getId());
    }

    @Test
    void shouldReturnEmptyList_WhenScheduledTimeIsAfterCutoff() {
        List<ActiveRide> result = activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                RideStatus.SCHEDULED,
                LocalDateTime.of(2024, 1, 1, 0, 0));
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyList_WhenStatusMatchesButNoScheduledTime() {
        List<ActiveRide> result = activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                RideStatus.ACTIVE,
                LocalDateTime.of(2099, 1, 1, 0, 0));
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnBothRides_WhenScheduledTimeEqualsLatestRide() {
        List<ActiveRide> result = activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                RideStatus.SCHEDULED,
                LocalDateTime.of(2030, 1, 1, 10, 0, 0));
        assertEquals(2, result.size());
    }

    // existsByPayingPassengerAndStatusNot

    @Test
    void shouldReturnTrue_WhenPassengerHasRideWithDifferentStatus() {
        assertTrue(activeRideRepository.existsByPayingPassengerAndStatusNot(passenger3, RideStatus.FINISHED));
    }

    @Test
    void shouldReturnFalse_WhenAllPassengerRidesMatchExcludedStatus() {
        assertFalse(activeRideRepository.existsByPayingPassengerAndStatusNot(passenger4, RideStatus.SCHEDULED));
    }

    @Test
    void shouldReturnFalse_WhenPassengerHasNoRidesAsPayingPassenger() {
        assertFalse(activeRideRepository.existsByPayingPassengerAndStatusNot(passenger5, RideStatus.FINISHED));
    }

    // existsByLinkedPassengersContainingAndStatusNot

    @Test
    void shouldReturnTrue_WhenLinkedPassengerHasRideWithDifferentStatus() {
        assertTrue(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(
                passenger4, RideStatus.FINISHED));
    }

    @Test
    void shouldReturnFalse_WhenLinkedPassengerRidesAllMatchExcludedStatus() {
        assertFalse(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(
                passenger4, RideStatus.ACTIVE));
    }

    @Test
    void shouldReturnFalse_WhenPassengerIsNotLinkedToAnyRide() {
        assertFalse(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(
                passenger3, RideStatus.FINISHED));
    }

    // existsByPayingPassengerAndStatusAndScheduledTimeBefore

    @Test
    void shouldReturnTrue_WhenPassengerHasScheduledRideBeforeTime() {
        assertTrue(activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                passenger4, RideStatus.SCHEDULED, LocalDateTime.of(2031, 1, 1, 0, 0)));
    }

    @Test
    void shouldReturnFalse_WhenScheduledTimeIsNotBeforeGivenTime() {
        assertFalse(activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                passenger4, RideStatus.SCHEDULED, LocalDateTime.of(2024, 1, 1, 0, 0)));
    }

    @Test
    void shouldReturnFalse_WhenPassengerHasNoRideWithGivenStatus() {
        assertFalse(activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                passenger4, RideStatus.ACTIVE, LocalDateTime.of(2031, 1, 1, 0, 0)));
    }

    @Test
    void shouldReturnFalse_WhenPassengerHasNoScheduledRides() {
        assertFalse(activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                passenger5, RideStatus.SCHEDULED, LocalDateTime.of(2031, 1, 1, 0, 0)));
    }

    // existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore

    @Test
    void shouldReturnTrue_WhenLinkedPassengerHasScheduledRideBeforeTime() {
        assertTrue(activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                passenger5, RideStatus.SCHEDULED, LocalDateTime.of(2026, 1, 1, 0, 0)));
    }

    @Test
    void shouldReturnFalse_WhenLinkedPassengerScheduledTimeIsNotBeforeGivenTime() {
        assertFalse(activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                passenger5, RideStatus.SCHEDULED, LocalDateTime.of(2024, 1, 1, 0, 0)));
    }

    @Test
    void shouldReturnFalse_WhenLinkedPassengerRideStatusDoesNotMatch() {
        assertFalse(activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                passenger4, RideStatus.SCHEDULED, LocalDateTime.of(2031, 1, 1, 0, 0)));
    }

    @Test
    void shouldReturnFalse_WhenPassengerIsNotLinkedToAnyScheduledRide() {
        assertFalse(activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                passenger3, RideStatus.SCHEDULED, LocalDateTime.of(2031, 1, 1, 0, 0)));
    }

    //    @Test
//    public void saveFindDeleteActiveRide() {
//        // Insert a user row for driver and driver row
//        KeyHolder khDriver = new GeneratedKeyHolder();
//        jdbcTemplate.update(con -> {
//            PreparedStatement ps = con.prepareStatement("INSERT INTO users (email, name, is_blocked) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
//            ps.setString(1, "drv@test.com");
//            ps.setString(2, "Test Driver");
//            ps.setBoolean(3, false);
//            return ps;
//        }, khDriver);
//        Long driverUserId = khDriver.getKey().longValue();
//        // insert into drivers table referencing the user id
//        jdbcTemplate.update("INSERT INTO drivers (id, is_activated, is_active) VALUES (?, ?, ?)", driverUserId, false, true);
//
//        // Insert a user row for passenger and passenger row
//        KeyHolder khPass = new GeneratedKeyHolder();
//        jdbcTemplate.update(con -> {
//            PreparedStatement ps = con.prepareStatement("INSERT INTO users (email, name, is_blocked) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
//            ps.setString(1, "pass@test.com");
//            ps.setString(2, "Test Passenger");
//            ps.setBoolean(3, false);
//            return ps;
//        }, khPass);
//        Long passUserId = khPass.getKey().longValue();
//        jdbcTemplate.update("INSERT INTO passengers (id, can_access_system) VALUES (?, ?)", passUserId, true);
//
//        // We inserted user/driver/passenger rows via JDBC; repository will load them.
//
//        // Insert ActiveRide row directly via JDBC to avoid transient entity issues
//        KeyHolder khRide = new GeneratedKeyHolder();
//        jdbcTemplate.update(con -> {
//            PreparedStatement ps = con.prepareStatement(
//                    "INSERT INTO active_rides (actual_start_time, estimated_duration_min, estimated_price, status, paying_passenger_id, driver_id, needs_baby_seats, needs_pet_friendly) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
//                    Statement.RETURN_GENERATED_KEYS);
//            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusMinutes(5)));
//            ps.setDouble(2, 30.0);
//            ps.setDouble(3, 100.0);
//            ps.setString(4, "ACTIVE");
//            ps.setLong(5, passUserId);
//            ps.setLong(6, driverUserId);
//            ps.setBoolean(7, false);
//            ps.setBoolean(8, false);
//            return ps;
//        }, khRide);
//
//        Long rideId = khRide.getKey().longValue();
//        assertNotNull(rideId);
//
//        // Load via repository
//        var found = activeRideRepository.findById(rideId);
//        assertTrue(found.isPresent());
//        ActiveRide loaded = found.get();
//        assertEquals(RideStatus.ACTIVE, loaded.getStatus());
//        assertEquals(100.0, loaded.getEstimatedPrice());
//
//        // verify findByStatus
//        List<ActiveRide> byStatus = activeRideRepository.findByStatus(RideStatus.ACTIVE);
//        assertFalse(byStatus.isEmpty());
//
//        // Cleanup
//        jdbcTemplate.update("DELETE FROM active_rides WHERE id = ?", rideId);
//        jdbcTemplate.update("DELETE FROM passengers WHERE id = ?", passUserId);
//        jdbcTemplate.update("DELETE FROM drivers WHERE id = ?", driverUserId);
//        jdbcTemplate.update("DELETE FROM users WHERE id IN (?, ?)", driverUserId, passUserId);
//    }

}
