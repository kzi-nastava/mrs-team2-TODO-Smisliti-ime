package rs.getgo.backend.S1.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.ride.CreateRideRequestDTO;
import rs.getgo.backend.dtos.ride.CreatedRideResponseDTO;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.mappers.RideMapper;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.NotificationType;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.*;
import rs.getgo.backend.services.DriverMatchingService;
import rs.getgo.backend.services.NotificationService;
import rs.getgo.backend.services.RidePriceService;
import rs.getgo.backend.services.impl.rides.MapboxRoutingService;
import rs.getgo.backend.services.impl.rides.RideOrderServiceImpl;
import rs.getgo.backend.validators.RideOrderValidator;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideOrderServiceImplTest {

    @Mock private WebSocketController webSocketController;
    @Mock private RouteRepository routeRepository;
    @Mock private ActiveRideRepository activeRideRepository;
    @Mock private RidePriceService ridePriceService;
    @Mock private MapboxRoutingService routingService;
    @Mock private DriverMatchingService driverMatchingService;
    @Mock private NotificationService notificationService;
    @Mock private RideMapper rideMapper;
    @Mock private RideOrderValidator rideOrderValidator;

    @InjectMocks
    private RideOrderServiceImpl rideOrderService;

    // Create simples ride: no waypoints, no linked passengers, not scheduled, no baby, pets, preferred vehicle type
    private CreateRideRequestDTO makeSimpleRideRequestDTO() {
        return new CreateRideRequestDTO(
                List.of(20.0, 30.0),
                List.of(20.0, 30.0),
                List.of("adr1", "adr2"),
                null,
                List.of(),
                false,
                false,
                null
        );
    }

    private Passenger makePassenger(Long id, String email) {
        Passenger passenger = new Passenger();
        passenger.setId(id);
        passenger.setEmail(email);
        return passenger;
    }

    private Driver makeDriver(Long id, String email, VehicleType vehicleType) {
        Vehicle vehicle = new Vehicle();
        vehicle.setType(vehicleType);

        Driver driver = new Driver();
        driver.setId(id);
        driver.setEmail(email);
        driver.setVehicle(vehicle);
        return driver;
    }

    // Mock repositories/services that are always called in identical ways
    private void mockStandardDependencies() {
        mockMapboxRouting();
        mockRouteRepository();
        mockRideMapper();
        mockNotificationService();
        mockRidePriceService();
    }

    private void mockMapboxRouting() {
        MapboxRoutingService.RouteResponse routeResponse = new MapboxRoutingService.RouteResponse(
                List.of(
                        new MapboxRoutingService.Coordinate(20.0, 20.0),
                        new MapboxRoutingService.Coordinate(25.0, 25.0),
                        new MapboxRoutingService.Coordinate(30.0, 30.0)
                ),
                100.0,
                1,
                2.0
        );
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeResponse);
        when(routingService.convertCoordinatesToJson(anyList()))
                .thenReturn("[[20.0,20.0],[25.0,25.0],[30.0,30.0]]");
    }

    private void mockRouteRepository() {
        when(routeRepository.save(any(Route.class)))
                .thenReturn(new Route());
    }

    private void mockRideMapper() {
        GetDriverActiveRideDTO driverDTO = new GetDriverActiveRideDTO();
        when(rideMapper.toDriverActiveRideDTO(any(ActiveRide.class)))
                .thenReturn(driverDTO);
    }

    private void mockNotificationService() {
        when(notificationService.createAndNotify(anyLong(), any(), anyString(), anyString(), any()))
                .thenReturn(new Notification());
    }

    private void mockRidePriceService() {
        when(ridePriceService.calculateRidePrice(any(), anyDouble()))
                .thenReturn(250.0);
    }

    private ArgumentCaptor<ActiveRide> mockActiveRideRepositoryWithCaptor() {
        ArgumentCaptor<ActiveRide> rideCaptor = ArgumentCaptor.forClass(ActiveRide.class);
        when(activeRideRepository.save(rideCaptor.capture()))
                .thenAnswer(invocation -> {
                    ActiveRide r = invocation.getArgument(0);
                    r.setId(100L);
                    return r;
                });
        return rideCaptor;
    }

    private void mockDriverNoActiveRide(Driver driver) {
        when(activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE))
                .thenReturn(false);
    }

    private void mockValidatorSuccess(Passenger payingPassenger, List<Passenger> linkedPassengers,
                                      LocalDateTime scheduledTime) {
        RideOrderValidator.ValidationResult validationResult =
                RideOrderValidator.ValidationResult.success(payingPassenger, linkedPassengers, scheduledTime);
        when(rideOrderValidator.validateRideOrder(any(CreateRideRequestDTO.class), anyString()))
                .thenReturn(validationResult);
    }

    // Simple order ride
    @Test
    void should_orderRide_when_allValid() {
        CreateRideRequestDTO dto = makeSimpleRideRequestDTO();

        Passenger passenger = makePassenger(1L, "passenger@gmail.com");
        mockValidatorSuccess(passenger, List.of(), null);

        mockStandardDependencies();

        Driver driver = makeDriver(10L, "driver@gmail.com", VehicleType.STANDARD);
        when(driverMatchingService.findAvailableDriver(any(ActiveRide.class)))
                .thenReturn(driver);
        mockDriverNoActiveRide(driver);

        ArgumentCaptor<ActiveRide> rideCaptor =mockActiveRideRepositoryWithCaptor();

        CreatedRideResponseDTO result = rideOrderService.orderRide(dto, "passenger@gmail.com");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Ride ordered successfully!", result.getMessage());
        assertEquals(100L, result.getRideId());

        ActiveRide savedRide = rideCaptor.getValue();
        assertEquals(RideStatus.DRIVER_READY, savedRide.getStatus());
        assertEquals(driver, savedRide.getDriver());
        assertEquals(passenger, savedRide.getPayingPassenger());
        assertEquals(250.0, savedRide.getEstimatedPrice());
        assertNull(savedRide.getScheduledTime());
        assertFalse(savedRide.isNeedsBabySeats());
        assertFalse(savedRide.isNeedsPetFriendly());
        assertEquals(VehicleType.STANDARD, savedRide.getVehicleType());

        Route savedRoute = rideCaptor.getValue().getRoute();
        assertEquals("adr1", savedRoute.getStartingPoint());
        assertEquals("adr2", savedRoute.getEndingPoint());
        assertEquals(2.0, savedRoute.getEstDistanceKm(), 0.01);
        assertEquals(2, savedRoute.getWaypoints().size());

        verify(webSocketController).notifyDriverRideAssigned(eq("driver@gmail.com"), any());
        verify(notificationService).createAndNotify(eq(10L), eq(NotificationType.DRIVER_ASSIGNED),
                anyString(), anyString(), any());
        verify(notificationService).createAndNotify(eq(1L), eq(NotificationType.RIDE_ORDERED),
                anyString(), anyString(), any());
    }

    // Simple order scheduled ride
    @Test
    void should_orderScheduledRide_when_allValid() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO(
                List.of(20.0, 30.0),
                List.of(20.0, 30.0),
                List.of("adr1", "adr2"),
                LocalTime.now().plusHours(2).format(DateTimeFormatter.ofPattern("HH:mm")),
                List.of(),
                false,
                false,
                null
        );

        Passenger passenger = makePassenger(1L, "passenger@gmail.com");
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(2);
        mockValidatorSuccess(passenger, List.of(), scheduledTime);

        mockMapboxRouting();
        mockRouteRepository();
        mockNotificationService();

        ArgumentCaptor<ActiveRide> rideCaptor =mockActiveRideRepositoryWithCaptor();

        CreatedRideResponseDTO result = rideOrderService.orderRide(dto, "passenger@gmail.com");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Ride scheduled successfully. Driver will be assigned closer to scheduled time.",
                result.getMessage());
        assertEquals(100L, result.getRideId());

        ActiveRide savedRide = rideCaptor.getValue();
        assertEquals(RideStatus.SCHEDULED, savedRide.getStatus());
        assertNull(savedRide.getDriver());
        assertEquals(0.0, savedRide.getEstimatedPrice());
        assertEquals(passenger, savedRide.getPayingPassenger());
        assertNotNull(savedRide.getScheduledTime());
        assertFalse(savedRide.isNeedsBabySeats());
        assertFalse(savedRide.isNeedsPetFriendly());

        Route savedRoute = savedRide.getRoute();
        assertEquals("adr1", savedRoute.getStartingPoint());
        assertEquals("adr2", savedRoute.getEndingPoint());
        assertEquals(2.0, savedRoute.getEstDistanceKm(), 0.01);

        verify(notificationService).createAndNotify(
                eq(1L), eq(NotificationType.RIDE_SCHEDULED), anyString(), anyString(), any());
        verify(driverMatchingService, never()).findAvailableDriver(any());
        verify(ridePriceService, never()).calculateRidePrice(any(), anyDouble());
        verify(webSocketController, never()).notifyDriverRideAssigned(anyString(), any());
    }

    // Simple order ride with multiple waypoints
    @Test
    void should_orderRide_when_hasMultipleWaypoints() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO(
                List.of(20.0, 25.0, 30.0, 35.0),
                List.of(20.0, 25.0, 30.0, 35.0),
                List.of("start", "stop1", "stop2", "end"),
                null,
                List.of(),
                false,
                false,
                null
        );

        Passenger passenger = makePassenger(1L, "passenger@gmail.com");
        mockValidatorSuccess(passenger, List.of(), null);

        mockStandardDependencies();

        MapboxRoutingService.RouteResponse routeResponse = new MapboxRoutingService.RouteResponse(
                List.of(
                        new MapboxRoutingService.Coordinate(20.0, 20.0),
                        new MapboxRoutingService.Coordinate(22.5, 22.5),
                        new MapboxRoutingService.Coordinate(25.0, 25.0)
                ),
                100.0, 1, 2.0
        );
        when(routingService.getRoute(20.0, 20.0, 25.0, 25.0))
                .thenReturn(routeResponse);
        when(routingService.getRoute(25.0, 25.0, 30.0, 30.0))
                .thenReturn(routeResponse);
        when(routingService.getRoute(30.0, 30.0, 35.0, 35.0))
                .thenReturn(routeResponse);
        when(routingService.convertCoordinatesToJson(anyList()))
                .thenReturn("[[20.0,20.0],[22.5,22.5],[25.0,25.0]]");

        Driver driver = makeDriver(10L, "driver@gmail.com", VehicleType.STANDARD);
        when(driverMatchingService.findAvailableDriver(any(ActiveRide.class)))
                .thenReturn(driver);
        mockDriverNoActiveRide(driver);

        ArgumentCaptor<ActiveRide> rideCaptor =mockActiveRideRepositoryWithCaptor();

        CreatedRideResponseDTO result = rideOrderService.orderRide(dto, "passenger@gmail.com");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(100L, result.getRideId());

        Route savedRoute = rideCaptor.getValue().getRoute();
        assertEquals(4, savedRoute.getWaypoints().size());
        assertEquals("start", savedRoute.getStartingPoint());
        assertEquals("end", savedRoute.getEndingPoint());
        assertEquals(6.0, savedRoute.getEstDistanceKm(), 0.01);
        assertEquals(5.0, savedRoute.getEstTimeMin(), 0.01);

        List<WayPoint> waypoints = savedRoute.getWaypoints();
        assertEquals(20.0, waypoints.getFirst().getLatitude());
        assertEquals(20.0, waypoints.get(0).getLongitude());
        assertEquals("start", waypoints.get(0).getAddress());

        assertEquals(25.0, waypoints.get(1).getLatitude());
        assertEquals(25.0, waypoints.get(1).getLongitude());
        assertEquals("stop1", waypoints.get(1).getAddress());

        assertEquals(30.0, waypoints.get(2).getLatitude());
        assertEquals(30.0, waypoints.get(2).getLongitude());
        assertEquals("stop2", waypoints.get(2).getAddress());

        assertEquals(35.0, waypoints.get(3).getLatitude());
        assertEquals(35.0, waypoints.get(3).getLongitude());
        assertEquals("end", waypoints.get(3).getAddress());

        verify(routingService, times(3)).getRoute(
                anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(ridePriceService).calculateRidePrice(any(), eq(6.0));
    }

    // Simple order ride with linked passengers. No active rides for passengers, drivers
    @Test
    void should_orderRide_when_hasLinkedPassengers() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO(
                List.of(20.0, 30.0),
                List.of(20.0, 30.0),
                List.of("adr1", "adr2"),
                null,
                List.of("friend1@gmail.com", "friend2@gmail.com"),
                false,
                false,
                null
        );

        Passenger passenger = makePassenger(1L, "passenger@gmail.com");
        Passenger linked1 = makePassenger(2L, "friend1@gmail.com");
        Passenger linked2 = makePassenger(3L, "friend2@gmail.com");
        mockValidatorSuccess(passenger, List.of(linked1, linked2), null);

        mockStandardDependencies();

        Driver driver = makeDriver(10L, "driver@gmail.com", VehicleType.STANDARD);
        when(driverMatchingService.findAvailableDriver(any(ActiveRide.class)))
                .thenReturn(driver);
        mockDriverNoActiveRide(driver);

        when(ridePriceService.calculateRidePrice(any(), anyDouble()))
                .thenReturn(250.0);

        ArgumentCaptor<ActiveRide> rideCaptor =mockActiveRideRepositoryWithCaptor();

        CreatedRideResponseDTO result = rideOrderService.orderRide(dto, "passenger@gmail.com");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(100L, result.getRideId());

        ActiveRide savedRide = rideCaptor.getValue();
        assertEquals(passenger, savedRide.getPayingPassenger());
        assertEquals(2, savedRide.getLinkedPassengers().size());
        assertTrue(savedRide.getLinkedPassengers().contains(linked1));
        assertTrue(savedRide.getLinkedPassengers().contains(linked2));
    }

    @Test
    void should_orderRide_when_needsBabySeatAndPetFriendly() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO(
                List.of(20.0, 30.0),
                List.of(20.0, 30.0),
                List.of("adr1", "adr2"),
                null,
                List.of(),
                true,
                true,
                null
        );

        Passenger passenger = makePassenger(1L, "passenger@gmail.com");
        mockValidatorSuccess(passenger, List.of(), null);

        mockStandardDependencies();

        Driver driver = makeDriver(10L, "driver@gmail.com", VehicleType.STANDARD);
        when(driverMatchingService.findAvailableDriver(any(ActiveRide.class)))
                .thenReturn(driver);
        mockDriverNoActiveRide(driver);

        ArgumentCaptor<ActiveRide> rideCaptor = mockActiveRideRepositoryWithCaptor();

        CreatedRideResponseDTO result = rideOrderService.orderRide(dto, "passenger@gmail.com");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(100L, result.getRideId());

        ActiveRide savedRide = rideCaptor.getValue();
        assertTrue(savedRide.isNeedsBabySeats());
        assertTrue(savedRide.isNeedsPetFriendly());
        assertEquals(passenger, savedRide.getPayingPassenger());
    }
}
