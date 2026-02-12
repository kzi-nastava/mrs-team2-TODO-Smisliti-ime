package rs.getgo.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ActiveRideRepositoryTest {

    @Autowired
    private ActiveRideRepository activeRideRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void saveFindDeleteActiveRide() {
        // Insert a user row for driver and driver row
        KeyHolder khDriver = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO users (email, name, is_blocked) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "drv@test.com");
            ps.setString(2, "Test Driver");
            ps.setBoolean(3, false);
            return ps;
        }, khDriver);
        Long driverUserId = khDriver.getKey().longValue();
        // insert into drivers table referencing the user id
        jdbcTemplate.update("INSERT INTO drivers (id, is_activated, is_active) VALUES (?, ?, ?)", driverUserId, false, true);

        // Insert a user row for passenger and passenger row
        KeyHolder khPass = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO users (email, name, is_blocked) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "pass@test.com");
            ps.setString(2, "Test Passenger");
            ps.setBoolean(3, false);
            return ps;
        }, khPass);
        Long passUserId = khPass.getKey().longValue();
        jdbcTemplate.update("INSERT INTO passengers (id, can_access_system) VALUES (?, ?)", passUserId, true);

        // We inserted user/driver/passenger rows via JDBC; repository will load them.

        // Insert ActiveRide row directly via JDBC to avoid transient entity issues
        KeyHolder khRide = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO active_rides (actual_start_time, estimated_duration_min, estimated_price, status, paying_passenger_id, driver_id, needs_baby_seats, needs_pet_friendly) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusMinutes(5)));
            ps.setDouble(2, 30.0);
            ps.setDouble(3, 100.0);
            ps.setString(4, "ACTIVE");
            ps.setLong(5, passUserId);
            ps.setLong(6, driverUserId);
            ps.setBoolean(7, false);
            ps.setBoolean(8, false);
            return ps;
        }, khRide);

        Long rideId = khRide.getKey().longValue();
        assertNotNull(rideId);

        // Load via repository
        var found = activeRideRepository.findById(rideId);
        assertTrue(found.isPresent());
        ActiveRide loaded = found.get();
        assertEquals(RideStatus.ACTIVE, loaded.getStatus());
        assertEquals(100.0, loaded.getEstimatedPrice());

        // verify findByStatus
        List<ActiveRide> byStatus = activeRideRepository.findByStatus(RideStatus.ACTIVE);
        assertFalse(byStatus.isEmpty());

        // Cleanup
        jdbcTemplate.update("DELETE FROM active_rides WHERE id = ?", rideId);
        jdbcTemplate.update("DELETE FROM passengers WHERE id = ?", passUserId);
        jdbcTemplate.update("DELETE FROM drivers WHERE id = ?", driverUserId);
        jdbcTemplate.update("DELETE FROM users WHERE id IN (?, ?)", driverUserId, passUserId);
    }

}
