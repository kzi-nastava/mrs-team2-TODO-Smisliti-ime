package rs.getgo.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.services.DriverService;
import rs.getgo.backend.services.impl.rides.DriverMatchingServiceImpl;
import rs.getgo.backend.services.impl.rides.MapboxRoutingService;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverMatchingServiceImplTest {

    @Mock private DriverRepository driverRepository;
    @Mock private ActiveRideRepository activeRideRepository;
    @Mock private DriverService driverService;
    @Mock private MapboxRoutingService routingService;

    @InjectMocks
    private DriverMatchingServiceImpl driverMatchingService;

    private ActiveRide makeRideWithStartPoint(double lat, double lng) {
        WayPoint startPoint = new WayPoint();
        startPoint.setLatitude(lat);
        startPoint.setLongitude(lng);

        Route route = new Route();
        route.setWaypoints(List.of(startPoint));

        ActiveRide ride = new ActiveRide();
        ride.setRoute(route);
        return ride;
    }

    private ActiveRide makeActiveRideWithEndPoint(double endLat, double endLng) {
        WayPoint endPoint = new WayPoint();
        endPoint.setLatitude(endLat);
        endPoint.setLongitude(endLng);
        endPoint.setReachedAt(null);

        Route route = new Route();
        route.setWaypoints(List.of(endPoint));

        ActiveRide activeRide = new ActiveRide();
        activeRide.setRoute(route);

        return activeRide;
    }

    private Driver makeDriver(Long id, double currentLat, double currentLng, VehicleType vehicleType) {
        Driver driver = new Driver();
        driver.setId(id);
        driver.setBlocked(false);
        driver.setCurrentLatitude(currentLat);
        driver.setCurrentLongitude(currentLng);

        Vehicle vehicle = new Vehicle();
        vehicle.setType(vehicleType);
        driver.setVehicle(vehicle);

        return driver;
    }

    private void mockDriverIsFinishingSoon(Driver driver, ActiveRide activeRide, double remainingMinutes) {
        when(activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE))
                .thenReturn(true);
        when(activeRideRepository.existsByDriverAndStatusIn(
                driver,
                List.of(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE, RideStatus.DRIVER_INCOMING, RideStatus.SCHEDULED)))
                .thenReturn(false);
        when(activeRideRepository.findByDriverAndStatus(driver, RideStatus.ACTIVE))
                .thenReturn(java.util.Optional.of(activeRide));

        WayPoint endPoint = activeRide.getRoute().getWaypoints().getFirst();
        when(routingService.calculateRemainingTime(
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude(),
                List.of(endPoint)))
                .thenReturn(remainingMinutes);
    }

    @Test
    void should_returnNull_when_noDriversAreActive() {
        ActiveRide ride = makeRideWithStartPoint(20.0, 30.0);

        when(driverRepository.findByIsActive(true))
                .thenReturn(new ArrayList<>());

        Driver result = driverMatchingService.findAvailableDriver(ride);

        assertNull(result);
    }

    @Test
    void should_returnNull_when_allDriversAreBlocked() {
        ActiveRide ride = makeRideWithStartPoint(20.0, 30.0);

        Driver driver1 = new Driver();
        driver1.setId(1L);
        driver1.setBlocked(true);

        Driver driver2 = new Driver();
        driver2.setId(2L);
        driver2.setBlocked(true);

        when(driverRepository.findByIsActive(true))
                .thenReturn(List.of(driver1, driver2));

        Driver result = driverMatchingService.findAvailableDriver(ride);

        assertNull(result);
    }

    @Test
    void should_returnNull_when_allDriversExceededWorkingHours() {
        ActiveRide ride = makeRideWithStartPoint(20.0, 30.0);

        Driver driver1 = new Driver();
        driver1.setId(1L);
        driver1.setBlocked(false);

        Driver driver2 = new Driver();
        driver2.setId(2L);
        driver2.setBlocked(false);

        when(driverRepository.findByIsActive(true))
                .thenReturn(List.of(driver1, driver2));
        when(driverService.hasExceededWorkingHours(driver1))
                .thenReturn(true);
        when(driverService.hasExceededWorkingHours(driver2))
                .thenReturn(true);

        Driver result = driverMatchingService.findAvailableDriver(ride);

        assertNull(result);
    }

    @Test
    void should_returnNull_when_noDriverHasRequiredVehicleType() {
        ActiveRide ride = makeRideWithStartPoint(20.0, 30.0);
        ride.setVehicleType(VehicleType.LUXURY);

        Driver driver = new Driver();
        driver.setId(1L);
        driver.setBlocked(false);
        driver.setCurrentLatitude(20.0);
        driver.setCurrentLongitude(30.0);

        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.STANDARD);
        driver.setVehicle(vehicle);

        when(driverRepository.findByIsActive(true))
                .thenReturn(List.of(driver));

        Driver result = driverMatchingService.findAvailableDriver(ride);

        assertNull(result);
    }

    @Test
    void should_returnNull_when_noDriverIsBabyFriendly() {
        ActiveRide ride = makeRideWithStartPoint(20.0, 30.0);
        ride.setNeedsBabySeats(true);

        Driver driver = new Driver();
        driver.setId(1L);
        driver.setBlocked(false);
        driver.setCurrentLatitude(20.0);
        driver.setCurrentLongitude(30.0);

        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setIsBabyFriendly(false);
        driver.setVehicle(vehicle);

        when(driverRepository.findByIsActive(true))
                .thenReturn(List.of(driver));

        Driver result = driverMatchingService.findAvailableDriver(ride);

        assertNull(result);
    }

    @Test
    void should_returnNull_when_noDriverIsPetFriendly() {
        ActiveRide ride = makeRideWithStartPoint(20.0, 30.0);
        ride.setNeedsPetFriendly(true);

        Driver driver = new Driver();
        driver.setId(1L);
        driver.setBlocked(false);
        driver.setCurrentLatitude(20.0);
        driver.setCurrentLongitude(30.0);

        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setIsPetFriendly(false);
        driver.setVehicle(vehicle);

        when(driverRepository.findByIsActive(true))
                .thenReturn(List.of(driver));

        Driver result = driverMatchingService.findAvailableDriver(ride);

        assertNull(result);
    }

    @Test
    void should_returnClosestFreeDriver_when_multipleFreeDriversAvailable() {
        ActiveRide ride = makeRideWithStartPoint(20.0, 30.0);

        Driver driver1 = new Driver();
        driver1.setId(1L);
        driver1.setBlocked(false);
        driver1.setCurrentLatitude(25.0);
        driver1.setCurrentLongitude(35.0);
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setType(VehicleType.STANDARD);
        driver1.setVehicle(vehicle1);

        Driver driver2 = new Driver();
        driver2.setId(2L);
        driver2.setBlocked(false);
        driver2.setCurrentLatitude(22.0);
        driver2.setCurrentLongitude(32.0);
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setType(VehicleType.STANDARD);
        driver2.setVehicle(vehicle2);

        Driver driver3 = new Driver();
        driver3.setId(3L);
        driver3.setBlocked(false);
        driver3.setCurrentLatitude(20.1);
        driver3.setCurrentLongitude(30.1);
        Vehicle vehicle3 = new Vehicle();
        vehicle3.setType(VehicleType.STANDARD);
        driver3.setVehicle(vehicle3);

        when(driverRepository.findByIsActive(true))
                .thenReturn(List.of(driver1, driver2, driver3));
        when(driverService.hasExceededWorkingHours(driver1))
                .thenReturn(false);
        when(driverService.hasExceededWorkingHours(driver2))
                .thenReturn(false);
        when(driverService.hasExceededWorkingHours(driver3))
                .thenReturn(false);
        when(activeRideRepository.existsByDriverAndStatusIn(any(Driver.class), any(List.class)))
                .thenReturn(false);

        Driver result = driverMatchingService.findAvailableDriver(ride);

        assertNotNull(result);
        assertEquals(3L, result.getId());
    }

    @Test
    void should_returnClosestDriver_when_driversFinishingSoonOnly() {
        ActiveRide ride = makeRideWithStartPoint(20.0, 30.0);

        Driver driver1 = makeDriver(1L, 22.0, 32.0, VehicleType.STANDARD);
        ActiveRide activeRide1 = makeActiveRideWithEndPoint(25.0, 35.0);

        Driver driver2 = makeDriver(2L, 20.5, 30.5, VehicleType.STANDARD);
        ActiveRide activeRide2 = makeActiveRideWithEndPoint(20.2, 30.2);

        when(driverRepository.findByIsActive(true))
                .thenReturn(List.of(driver1, driver2));
        when(driverService.hasExceededWorkingHours(any(Driver.class)))
                .thenReturn(false);

        when(activeRideRepository.existsByDriverAndStatusIn(any(Driver.class), any(List.class)))
                .thenReturn(true);

        mockDriverIsFinishingSoon(driver1, activeRide1, 8.0);
        mockDriverIsFinishingSoon(driver2, activeRide2, 5.0);

        Driver result = driverMatchingService.findAvailableDriver(ride);

        assertNotNull(result);
        assertEquals(2L, result.getId());
    }

    @Test
    void should_returnNull_when_driversHaveScheduledRides() {
        ActiveRide ride = makeRideWithStartPoint(20.0, 30.0);

        Driver driver1 = makeDriver(1L, 20.1, 30.1, VehicleType.STANDARD);

        when(driverRepository.findByIsActive(true))
                .thenReturn(List.of(driver1));
        when(driverService.hasExceededWorkingHours(driver1))
                .thenReturn(false);

        when(activeRideRepository.existsByDriverAndStatusIn(
                driver1,
                List.of(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE, RideStatus.DRIVER_READY,
                        RideStatus.DRIVER_INCOMING, RideStatus.DRIVER_ARRIVED,
                        RideStatus.ACTIVE, RideStatus.DRIVER_ARRIVED_AT_DESTINATION)))
                .thenReturn(true);

        when(activeRideRepository.existsByDriverAndStatus(driver1, RideStatus.ACTIVE))
                .thenReturn(true);
        when(activeRideRepository.existsByDriverAndStatusIn(
                driver1,
                List.of(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE, RideStatus.DRIVER_INCOMING, RideStatus.SCHEDULED)))
                .thenReturn(true);

        Driver result = driverMatchingService.findAvailableDriver(ride);

        assertNull(result);
    }

    @Test
    void should_returnNull_when_driversNotFinishingSoon() {
        ActiveRide ride = makeRideWithStartPoint(20.0, 30.0);

        Driver driver1 = makeDriver(1L, 20.1, 30.1, VehicleType.STANDARD);
        ActiveRide activeRide1 = makeActiveRideWithEndPoint(25.0, 35.0);

        when(driverRepository.findByIsActive(true))
                .thenReturn(List.of(driver1));
        when(driverService.hasExceededWorkingHours(driver1))
                .thenReturn(false);

        when(activeRideRepository.existsByDriverAndStatusIn(
                driver1,
                List.of(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE, RideStatus.DRIVER_READY,
                        RideStatus.DRIVER_INCOMING, RideStatus.DRIVER_ARRIVED,
                        RideStatus.ACTIVE, RideStatus.DRIVER_ARRIVED_AT_DESTINATION)))
                .thenReturn(true);

        when(activeRideRepository.existsByDriverAndStatus(driver1, RideStatus.ACTIVE))
                .thenReturn(true);
        when(activeRideRepository.existsByDriverAndStatusIn(
                driver1,
                List.of(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE, RideStatus.DRIVER_INCOMING, RideStatus.SCHEDULED)))
                .thenReturn(false);
        when(activeRideRepository.findByDriverAndStatus(driver1, RideStatus.ACTIVE))
                .thenReturn(java.util.Optional.of(activeRide1));

        WayPoint endPoint = activeRide1.getRoute().getWaypoints().getFirst();
        when(routingService.calculateRemainingTime(
                driver1.getCurrentLatitude(),
                driver1.getCurrentLongitude(),
                List.of(endPoint)))
                .thenReturn(15.0);

        Driver result = driverMatchingService.findAvailableDriver(ride);

        assertNull(result);
    }
}