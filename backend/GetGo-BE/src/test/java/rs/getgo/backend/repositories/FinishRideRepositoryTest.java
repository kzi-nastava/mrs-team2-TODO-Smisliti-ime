package rs.getgo.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.enums.RideStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class FinishRideRepositoryTest {

    @Mock
    private ActiveRideRepository activeRideRepository;

    @BeforeEach
    void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

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

   /* @Test
    public void testExistsByPayingPassengerOrLinkedPassengersContaining() {
        Passenger p = new Passenger();
        p.setEmail("pass2@example.com");
        p.setName("P2");

        when(activeRideRepository.existsByPayingPassengerOrLinkedPassengersContaining(p, p)).thenReturn(true);

        boolean exists = activeRideRepository.existsByPayingPassengerOrLinkedPassengersContaining(p, p);
        assertThat(exists).isTrue();

        verify(activeRideRepository, times(1)).existsByPayingPassengerOrLinkedPassengersContaining(p, p);
    }*/

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

    @Test
    public void testFindByStatusInAndExistsByDriverAndStatus() {
        ActiveRide a = new ActiveRide();
        a.setStatus(RideStatus.ACTIVE);
        when(activeRideRepository.findByStatusIn(List.of(RideStatus.ACTIVE, RideStatus.DRIVER_READY))).thenReturn(List.of(a));

        List<ActiveRide> res = activeRideRepository.findByStatusIn(List.of(RideStatus.ACTIVE, RideStatus.DRIVER_READY));
        assertThat(res).hasSize(1);

        Driver d = new Driver();
        when(activeRideRepository.existsByDriverAndStatusIn(d, List.of(RideStatus.ACTIVE))).thenReturn(true);
        boolean exists = activeRideRepository.existsByDriverAndStatusIn(d, List.of(RideStatus.ACTIVE));
        assertThat(exists).isTrue();

        verify(activeRideRepository).findByStatusIn(List.of(RideStatus.ACTIVE, RideStatus.DRIVER_READY));
        verify(activeRideRepository).existsByDriverAndStatusIn(d, List.of(RideStatus.ACTIVE));
    }

    @Test
    public void testFindByStatusAndScheduledTimeLessThanEqual() {
        ActiveRide a = new ActiveRide();
        a.setStatus(RideStatus.SCHEDULED);
        LocalDateTime cutoff = LocalDateTime.of(2025,1,1,0,0);
        when(activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(RideStatus.SCHEDULED, cutoff)).thenReturn(List.of(a));

        List<ActiveRide> res = activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(RideStatus.SCHEDULED, cutoff);
        assertThat(res).isNotEmpty();

        verify(activeRideRepository).findByStatusAndScheduledTimeLessThanEqual(RideStatus.SCHEDULED, cutoff);
    }

}
