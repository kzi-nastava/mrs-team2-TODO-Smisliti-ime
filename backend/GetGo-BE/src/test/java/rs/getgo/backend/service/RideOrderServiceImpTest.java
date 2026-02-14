package rs.getgo.backend.service;

import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideOrderServiceImplTest {

    @Mock private WebSocketController webSocketController;
    @Mock private PassengerRepository passengerRepository;
    @Mock private BlockNoteRepository blockNoteRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private ActiveRideRepository activeRideRepository;
    @Mock private RidePriceService ridePriceService;
    @Mock private MapboxRoutingService routingService;
    @Mock private DriverMatchingService driverMatchingService;
    @Mock private NotificationService notificationService;
    @Mock private RideMapper rideMapper;

    @InjectMocks
    private RideOrderServiceImpl rideOrderService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void should_orderRide_when_allValid() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO(
                List.of(20.0, 30.0),
                List.of(20.0, 30.0),
                List.of("adr1", "adr2"),
                null,
                List.of(),
                false,
                false,
                null
        );

        Passenger passenger = new Passenger();
        passenger.setId(1L);
        passenger.setEmail("passenger@gmail.com");
        passenger.setBlocked(false);
        when(passengerRepository.findByEmail("passenger@gmail.com"))
                .thenReturn(Optional.of(passenger));

        when(activeRideRepository.existsByPayingPassengerAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                eq(passenger), eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                eq(passenger), eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(false);

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
        when(routingService.getRoute(20.0, 20.0, 30.0, 30.0))
                .thenReturn(routeResponse);
        when(routingService.convertCoordinatesToJson(anyList()))
                .thenReturn("[[20.0,20.0],[25.0,25.0],[30.0,30.0]]");

        when(routeRepository.save(any(Route.class)))
                .thenAnswer(invocation -> {
                    Route r = invocation.getArgument(0);
                    r.setId(1L);
                    return r;
                });

        Driver driver = new Driver();
        driver.setId(10L);
        driver.setEmail("driver@gmail.com");
        when(driverMatchingService.findAvailableDriver(any(ActiveRide.class)))
                .thenReturn(driver);

        when(activeRideRepository.existsByDriverAndStatus(driver, RideStatus.ACTIVE))
                .thenReturn(false);

        when(ridePriceService.calculateRidePrice(any(), anyDouble()))
                .thenReturn(250.0);

        ArgumentCaptor<ActiveRide> rideCaptor = ArgumentCaptor.forClass(ActiveRide.class);
        when(activeRideRepository.save(rideCaptor.capture()))
                .thenAnswer(invocation -> {
                    ActiveRide r = invocation.getArgument(0);
                    r.setId(100L);
                    return r;
                });

        GetDriverActiveRideDTO driverDTO = new GetDriverActiveRideDTO();
        when(rideMapper.toDriverActiveRideDTO(any(ActiveRide.class)))
                .thenReturn(driverDTO);

        doNothing().when(webSocketController).notifyDriverRideAssigned(anyString(), any());
        when(notificationService.createAndNotify(anyLong(), any(), anyString(), anyString(), any()))
                .thenReturn(new Notification());


        CreatedRideResponseDTO result = rideOrderService.orderRide(dto, "passenger@gmail.com");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Ride ordered successfully!", result.getMessage());
        assertEquals(100L, result.getRideId());

        Route savedRoute = rideCaptor.getValue().getRoute();
        assertEquals("adr1", savedRoute.getStartingPoint());
        assertEquals("adr2", savedRoute.getEndingPoint());
        assertEquals(2.0, savedRoute.getEstDistanceKm(), 0.01);
        assertEquals(2, savedRoute.getWaypoints().size());

        ActiveRide savedRide = rideCaptor.getValue();
        assertEquals(RideStatus.DRIVER_READY, savedRide.getStatus());
        assertEquals(driver, savedRide.getDriver());
        assertEquals(passenger, savedRide.getPayingPassenger());
        assertEquals(250.0, savedRide.getEstimatedPrice());
        assertNull(savedRide.getScheduledTime());
        assertFalse(savedRide.isNeedsBabySeats());
        assertFalse(savedRide.isNeedsPetFriendly());
    }


}







































