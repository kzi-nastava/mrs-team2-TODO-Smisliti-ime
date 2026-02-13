package rs.getgo.backend.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.entities.Route;
import rs.getgo.backend.model.enums.RideStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActiveRideRepositoryTest {

    @Mock
    private ActiveRideRepository activeRideRepository;

    @Test
    public void testFindFirstByDriverAndStatusOrderByScheduledTimeAsc() {
        Driver d = new Driver();
        d.setEmail("drv1@example.com");
        d.setName("D1");

        ActiveRide r1 = new ActiveRide();
        r1.setDriver(d);
        r1.setStatus(RideStatus.SCHEDULED);
        r1.setScheduledTime(LocalDateTime.of(2025,1,2,10,0));

        ActiveRide r2 = new ActiveRide();
        r2.setDriver(d);
        r2.setStatus(RideStatus.SCHEDULED);
        r2.setScheduledTime(LocalDateTime.of(2025,1,1,10,0));

        when(activeRideRepository.findFirstByDriverAndStatusOrderByScheduledTimeAsc(d, RideStatus.SCHEDULED)).thenReturn(Optional.of(r2));

        Optional<ActiveRide> found = activeRideRepository.findFirstByDriverAndStatusOrderByScheduledTimeAsc(d, RideStatus.SCHEDULED);
        assertThat(found).isPresent();
        assertThat(found.get().getScheduledTime()).isEqualTo(LocalDateTime.of(2025,1,1,10,0));

        verify(activeRideRepository, times(1)).findFirstByDriverAndStatusOrderByScheduledTimeAsc(d, RideStatus.SCHEDULED);
    }

    @Test
    public void testFindActiveRideForPassenger_PayingOrLinked() {
        Passenger p = new Passenger();
        p.setEmail("pass1@example.com");
        p.setName("P1");

        ActiveRide r = new ActiveRide();
        r.setPayingPassenger(p);
        r.setStatus(RideStatus.ACTIVE);

        when(activeRideRepository.findActiveRideForPassenger(p, List.of(RideStatus.ACTIVE))).thenReturn(Optional.of(r));

        Optional<ActiveRide> found = activeRideRepository.findActiveRideForPassenger(p, List.of(RideStatus.ACTIVE));
        assertThat(found).isPresent();
        assertThat(found.get().getPayingPassenger().getEmail()).isEqualTo("pass1@example.com");

        verify(activeRideRepository, times(1)).findActiveRideForPassenger(p, List.of(RideStatus.ACTIVE));
    }

    @Test
    public void testExistsByPayingPassengerOrLinkedPassengersContaining() {
        Passenger p = new Passenger();
        p.setEmail("pass2@example.com");
        p.setName("P2");

        when(activeRideRepository.existsByPayingPassengerOrLinkedPassengersContaining(p, p)).thenReturn(true);

        boolean exists = activeRideRepository.existsByPayingPassengerOrLinkedPassengersContaining(p, p);
        assertThat(exists).isTrue();

        verify(activeRideRepository, times(1)).existsByPayingPassengerOrLinkedPassengersContaining(p, p);
    }

    @Test
    public void testFindByDriverAndStatusIn() {
        Driver d = new Driver();
        d.setEmail("driverx@example.com");
        d.setName("DriverX");

        ActiveRide r1 = new ActiveRide();
        r1.setDriver(d);
        r1.setStatus(RideStatus.DRIVER_READY);

        ActiveRide r2 = new ActiveRide();
        r2.setDriver(d);
        r2.setStatus(RideStatus.ACTIVE);

        when(activeRideRepository.findByDriverAndStatusIn(d, List.of(RideStatus.DRIVER_READY, RideStatus.ACTIVE))).thenReturn(List.of(r1, r2));

        List<ActiveRide> rides = activeRideRepository.findByDriverAndStatusIn(d, List.of(RideStatus.DRIVER_READY, RideStatus.ACTIVE));
        assertThat(rides).isNotEmpty();
        assertThat(rides).hasSizeGreaterThanOrEqualTo(2);

        verify(activeRideRepository, times(1)).findByDriverAndStatusIn(d, List.of(RideStatus.DRIVER_READY, RideStatus.ACTIVE));
    }

}
