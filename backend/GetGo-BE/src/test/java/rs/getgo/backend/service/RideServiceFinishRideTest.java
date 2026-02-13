package rs.getgo.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.ride.UpdateRideDTO;
import rs.getgo.backend.dtos.ride.UpdatedRideDTO;
import rs.getgo.backend.mappers.RideMapper;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.*;
import rs.getgo.backend.services.*;
import rs.getgo.backend.services.impl.rides.MapboxRoutingService;
import rs.getgo.backend.services.impl.rides.RideServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideServiceFinishRideTest {

    @Mock
    private RideCancellationRepository cancellationRepository;

    @Mock
    private PanicRepository panicRepository;

    @Mock
    private ActiveRideRepository activeRideRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private MapboxRoutingService mapboxRoutingService;

    @Mock
    private WebSocketController webSocketController;

    @Mock
    private CompletedRideRepository completedRideRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private InconsistencyReportRepository reportRepository;

    @Mock
    private PanicNotifierService panicNotifierService;

    @Mock
    private RideMapper rideMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RidePriceRepository ridePriceRepository;

    @InjectMocks
    private RideServiceImpl rideService;

    private ActiveRide activeRide;
    private Driver driver;
    private Passenger payingPassenger;

    @BeforeEach
    void setUp() {
        // Build a minimal ActiveRide in ACTIVE status
        activeRide = new ActiveRide();
        activeRide.setId(11L);

        Route route = new Route();
        route.setId(21L);
        route.setEstTimeMin(30.0);
        route.setEstDistanceKm(12.0);
        route.setWaypoints(new java.util.LinkedList<>());
        activeRide.setRoute(route);

        activeRide.setEstimatedPrice(200.0);
        // Use a fixed start time to avoid flakiness
        LocalDateTime fixedStart = LocalDateTime.of(2025, 1, 1, 10, 0);
        activeRide.setActualStartTime(fixedStart);
        activeRide.setStatus(RideStatus.ACTIVE);

        driver = new Driver();
        driver.setId(31L);
        driver.setEmail("drv@example.com");
        driver.setName("Drv");
        driver.setActive(false);
        activeRide.setDriver(driver);

        payingPassenger = new Passenger();
        payingPassenger.setId(41L);
        payingPassenger.setEmail("pass@example.com");
        payingPassenger.setName("Pass");
        payingPassenger.setSurname("Surname");
        activeRide.setPayingPassenger(payingPassenger);

        activeRide.setLinkedPassengers(List.of());
    }

    @Test
    public void testFinishRide_HappyPath() {
        // Arrange
        when(activeRideRepository.findById(11L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(500L);
            return cr;
        });
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());
        when(activeRideRepository.findFirstByDriverAndStatusOrderByScheduledTimeAsc(any(Driver.class), any())).thenReturn(Optional.empty());
        when(panicRepository.findByRideId(11L)).thenReturn(List.of());

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(11L, req);

        // Assert
        assertNotNull(result);
        assertEquals(500L, result.getId());
        assertEquals("FINISHED", result.getStatus());
        assertNotNull(result.getEndTime());

        verify(completedRideRepository, times(1)).save(any(CompletedRide.class));
        verify(emailService, times(1)).sendRideFinishedEmail(eq(payingPassenger.getEmail()), eq(payingPassenger.getName()), eq(500L), eq(payingPassenger.getId()));
        verify(webSocketController, times(1)).notifyDriverRideFinished(eq(driver.getEmail()), eq(activeRide.getId()), anyDouble(), any(LocalDateTime.class), any(LocalDateTime.class), eq(driver.getId()));
        verify(webSocketController, times(1)).notifyPassengerRideFinished(eq(activeRide.getId()), anyDouble(), any(LocalDateTime.class), any(LocalDateTime.class), eq(driver.getId()));
        verify(activeRideRepository, times(1)).delete(activeRide);
        // driverRepository.save should be called to mark driver active (no scheduled rides => active true)
        verify(driverRepository, times(1)).save(argThat(d -> ((Driver) d).getActive()));
    }

    @Test
    public void testFinishRide_RideNotFound() {
        when(activeRideRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateRideDTO req = new UpdateRideDTO();
        assertThrows(IllegalStateException.class, () -> rideService.finishRide(99L, req));
    }

    @Test
    public void testFinishRide_InvalidStatus_Throws() {
        activeRide.setStatus(RideStatus.DRIVER_READY);
        when(activeRideRepository.findById(11L)).thenReturn(Optional.of(activeRide));
        UpdateRideDTO req = new UpdateRideDTO();
        assertThrows(IllegalStateException.class, () -> rideService.finishRide(11L, req));
    }

    @Test
    public void testFinishRide_PanicsLinkedToCompletedRide() {
        // Arrange
        when(activeRideRepository.findById(11L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(777L);
            return cr;
        });
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());

        Panic panic = new Panic();
        panic.setId(900L);
        // Return a single present Optional
        when(panicRepository.findByRideId(11L)).thenReturn(List.of(Optional.of(panic)));

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(11L, req);

        // Assert
        assertNotNull(result);
        assertEquals(777L, result.getId());
        // panic object should have been mutated to point to completed ride id
        assertEquals(777L, panic.getRideId());
        // completedRideRepository.save should have been called at least twice (initial save + update when panic attached)
        verify(completedRideRepository, atLeast(2)).save(any(CompletedRide.class));
    }
}

