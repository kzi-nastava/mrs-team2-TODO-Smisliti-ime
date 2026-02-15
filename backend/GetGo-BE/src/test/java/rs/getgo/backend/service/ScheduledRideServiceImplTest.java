package rs.getgo.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.mappers.RideMapper;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.NotificationType;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.RideCancellationRepository;
import rs.getgo.backend.services.DriverMatchingService;
import rs.getgo.backend.services.NotificationService;
import rs.getgo.backend.services.RidePriceService;
import rs.getgo.backend.services.impl.rides.ScheduledRideService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledRideServiceTest {

    @Mock private ActiveRideRepository activeRideRepository;
    @Mock private RideCancellationRepository rideCancellationRepository;
    @Mock private DriverMatchingService driverMatchingService;
    @Mock private RidePriceService ridePriceService;
    @Mock private NotificationService notificationService;
    @Mock private WebSocketController webSocketController;
    @Mock private RideMapper rideMapper;

    @InjectMocks
    private ScheduledRideService scheduledRideService;

    private ActiveRide makeScheduledRide(Long rideId, Long passengerId, LocalDateTime scheduledTime) {
        Passenger passenger = new Passenger();
        passenger.setId(passengerId);

        Route route = new Route();
        route.setEstDistanceKm(10.0);

        ActiveRide ride = new ActiveRide();
        ride.setId(rideId);
        ride.setPayingPassenger(passenger);
        ride.setScheduledTime(scheduledTime);
        ride.setStatus(RideStatus.SCHEDULED);
        ride.setRoute(route);

        return ride;
    }

    private Driver makeDriver(Long id, VehicleType vehicleType) {
        Driver driver = new Driver();
        driver.setId(id);
        driver.setEmail("driver" + id + "@test.com");

        Vehicle vehicle = new Vehicle();
        vehicle.setType(vehicleType);
        driver.setVehicle(vehicle);

        return driver;
    }

    @Test
    void should_sendReminder_when_scheduledRideAndNoRecentNotification() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime reminderWindow = now.plusMinutes(15);
        LocalDateTime scheduledTime = now.plusMinutes(10);

        ActiveRide ride = makeScheduledRide(1L, 100L, scheduledTime);

        when(activeRideRepository.findByStatusAndScheduledTimeBetween(
                RideStatus.SCHEDULED, now, reminderWindow))
                .thenReturn(List.of(ride));
        when(notificationService.wasRecentlySent(100L, NotificationType.RIDE_STARTING_SOON, 4))
                .thenReturn(false);

        scheduledRideService.handleRideReminders(now, reminderWindow);

        verify(notificationService).createAndNotify(
                eq(100L),
                eq(NotificationType.RIDE_STARTING_SOON),
                eq("Ride reminder"),
                eq("Your scheduled ride starts at " + scheduledTime.toLocalTime()),
                any(LocalDateTime.class)
        );
    }

    @Test
    void should_notSendReminder_when_scheduledRideAndRecentNotification() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime reminderWindow = now.plusMinutes(15);
        LocalDateTime scheduledTime = now.plusMinutes(10);

        ActiveRide ride = makeScheduledRide(1L, 100L, scheduledTime);

        when(activeRideRepository.findByStatusAndScheduledTimeBetween(
                RideStatus.SCHEDULED, now, reminderWindow))
                .thenReturn(List.of(ride));
        when(notificationService.wasRecentlySent(100L, NotificationType.RIDE_STARTING_SOON, 4))
                .thenReturn(true);

        scheduledRideService.handleRideReminders(now, reminderWindow);

        verify(notificationService, never()).createAndNotify(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    void should_notSendReminder_when_noScheduledRides() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime reminderWindow = now.plusMinutes(15);

        when(activeRideRepository.findByStatusAndScheduledTimeBetween(
                RideStatus.SCHEDULED, now, reminderWindow))
                .thenReturn(List.of());

        scheduledRideService.handleRideReminders(now, reminderWindow);

        verify(notificationService, never()).createAndNotify(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    void should_doNothing_when_noScheduledRidesReadyForActivation() {
        when(activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduledRideService.activateScheduledRides();

        verify(driverMatchingService, never()).findAvailableDriver(any());
        verify(activeRideRepository, never()).save(any());
    }

    @Test
    void should_notActivateRide_when_noDriverAvailable() {
        ActiveRide ride = makeScheduledRide(1L, 100L, LocalDateTime.now().plusMinutes(10));

        when(activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of(ride));
        when(driverMatchingService.findAvailableDriver(ride))
                .thenReturn(null);

        scheduledRideService.activateScheduledRides();

        verify(activeRideRepository, never()).save(any());
        verify(notificationService, never()).createAndNotify(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    void should_activateRideWithStatusReady_when_driverFreeAndVehicleTypeSet() {
        ActiveRide ride = makeScheduledRide(1L, 100L, LocalDateTime.now().plusMinutes(10));
        ride.setVehicleType(VehicleType.STANDARD);
        Driver driver = makeDriver(1L, VehicleType.STANDARD);

        when(activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of(ride));
        when(driverMatchingService.findAvailableDriver(ride))
                .thenReturn(driver);
        when(activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE))
                .thenReturn(false);

        scheduledRideService.activateScheduledRides();

        verify(activeRideRepository).save(ride);
        verify(notificationService).createAndNotify(
                eq(1L), eq(NotificationType.DRIVER_ASSIGNED), any(), any(), any()
        );
        verify(notificationService).createAndNotify(
                eq(100L), eq(NotificationType.RIDE_ACCEPTED), any(), any(), any()
        );
        verify(webSocketController).notifyPassengerRideStatusUpdate(
                eq(1L), eq(RideStatus.DRIVER_READY.toString()), any()
        );
    }

    @Test
    void should_activateRideWithStatusFinishingPrevious_when_driverHasActiveRide() {
        ActiveRide ride = makeScheduledRide(1L, 100L, LocalDateTime.now().plusMinutes(10));
        ride.setVehicleType(VehicleType.STANDARD);
        Driver driver = makeDriver(1L, VehicleType.STANDARD);

        when(activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of(ride));
        when(driverMatchingService.findAvailableDriver(ride))
                .thenReturn(driver);
        when(activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE))
                .thenReturn(true);

        scheduledRideService.activateScheduledRides();

        verify(activeRideRepository).save(ride);
        verify(notificationService).createAndNotify(
                eq(1L), eq(NotificationType.DRIVER_ASSIGNED), any(), any(), any()
        );
        verify(notificationService).createAndNotify(
                eq(100L), eq(NotificationType.RIDE_ACCEPTED), any(), any(), any()
        );
        verify(webSocketController).notifyPassengerRideStatusUpdate(
                eq(1L), eq(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE.toString()), any()
        );
    }

    @Test
    void should_setVehicleTypeAndPrice_when_vehicleTypeNotSet() {
        ActiveRide ride = makeScheduledRide(1L, 100L, LocalDateTime.now().plusMinutes(10));
        ride.setVehicleType(null);
        Driver driver = makeDriver(1L, VehicleType.LUXURY);

        when(activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of(ride));
        when(driverMatchingService.findAvailableDriver(ride))
                .thenReturn(driver);
        when(activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE))
                .thenReturn(false);
        when(ridePriceService.calculateRidePrice(VehicleType.LUXURY, 10.0))
                .thenReturn(25.0);

        scheduledRideService.activateScheduledRides();

        verify(ridePriceService).calculateRidePrice(VehicleType.LUXURY, 10.0);
        verify(activeRideRepository).save(ride);
    }

    @Test
    void should_activateMultipleRides_when_multipleScheduled() {
        ActiveRide ride1 = makeScheduledRide(1L, 100L, LocalDateTime.now().plusMinutes(10));
        ride1.setVehicleType(VehicleType.STANDARD);
        ActiveRide ride2 = makeScheduledRide(2L, 200L, LocalDateTime.now().plusMinutes(12));
        ride2.setVehicleType(VehicleType.STANDARD);

        Driver driver1 = makeDriver(1L, VehicleType.STANDARD);
        Driver driver2 = makeDriver(2L, VehicleType.STANDARD);

        when(activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of(ride1, ride2));
        when(driverMatchingService.findAvailableDriver(ride1))
                .thenReturn(driver1);
        when(driverMatchingService.findAvailableDriver(ride2))
                .thenReturn(driver2);
        when(activeRideRepository.existsByDriverAndStatus(any(), eq(RideStatus.ACTIVE)))
                .thenReturn(false);

        scheduledRideService.activateScheduledRides();

        verify(activeRideRepository, times(2)).save(any());
        verify(notificationService, times(4)).createAndNotify(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    void should_cancelNoRides_when_noOverdueRides() {
        when(activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduledRideService.cancelOverdueScheduledRides();

        verify(rideCancellationRepository, never()).save(any());
        verify(activeRideRepository, never()).delete(any());
        verify(notificationService, never()).createAndNotify(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    void should_cancelRide_when_rideOverdue() {
        ActiveRide ride = makeScheduledRide(1L, 100L, LocalDateTime.now().minusMinutes(10));

        when(activeRideRepository.findByStatusAndScheduledTimeLessThanEqual(
                eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of(ride));

        scheduledRideService.cancelOverdueScheduledRides();

        verify(rideCancellationRepository).save(any(RideCancellation.class));
        verify(activeRideRepository).delete(ride);
        verify(notificationService).createAndNotify(
                eq(100L),
                eq(NotificationType.RIDE_REJECTED),
                eq("Scheduled ride cancelled"),
                any(),
                any(LocalDateTime.class)
        );
    }
}