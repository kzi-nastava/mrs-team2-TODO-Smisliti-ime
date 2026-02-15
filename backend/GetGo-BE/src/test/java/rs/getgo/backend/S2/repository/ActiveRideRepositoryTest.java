package rs.getgo.backend.S2.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.GetGoBeApplication;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = {"/sql/S2/active-ride-s2.sql"})
public class ActiveRideRepositoryTest {

    @Autowired
    private ActiveRideRepository activeRideRepository;

    @Test
    public void fixture_shouldLoadActiveRide() {
        Optional<ActiveRide> ar = activeRideRepository.findById(101L);
        assertThat(ar).isPresent();
        assertThat(ar.get().getEstimatedPrice()).isEqualTo(150.0);
    }

    @Test
    public void shouldFindByStatusActive() {
        List<ActiveRide> list = activeRideRepository.findByStatus(RideStatus.ACTIVE);
        // our S2 fixture has one active ride
        assertThat(list).isNotEmpty();
        assertThat(list.stream().anyMatch(r -> r.getId().equals(101L))).isTrue();
    }

    @Test
    public void shouldExistByDriverAndStatusIn() {
        // load the active ride and get the driver from the relation instead of autowiring DriverRepository
        ActiveRide ar = activeRideRepository.findById(101L).orElseThrow();
        Driver drv = ar.getDriver();
        boolean exists = activeRideRepository.existsByDriverAndStatusIn(drv, List.of(RideStatus.ACTIVE));
        assertThat(exists).isTrue();
    }

    @Test
    public void shouldFindByPayingPassengerAndStatusIn() {
        // get passenger via the active ride relation instead of autowiring PassengerRepository
        ActiveRide ar = activeRideRepository.findById(101L).orElseThrow();
        Passenger p = ar.getPayingPassenger();
        Optional<ActiveRide> opt = activeRideRepository.findActiveRideForPassenger(p, List.of(RideStatus.ACTIVE));
        assertThat(opt).isPresent();
        assertThat(opt.get().getId()).isEqualTo(101L);
    }

    @Test
    public void shouldReturnEmptyForOtherStatus() {
        List<ActiveRide> res = activeRideRepository.findByStatus(RideStatus.SCHEDULED);
        // S2 fixture has no scheduled rides
        assertThat(res).isEmpty();
    }

}

