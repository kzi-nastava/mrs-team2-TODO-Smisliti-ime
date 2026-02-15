package rs.getgo.backend.S2.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class FinishRideServiceTest {

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

    private AutoCloseable mocksCloser;

    @BeforeEach
    void setUp() {
        mocksCloser = MockitoAnnotations.openMocks(this);
        // reference mocks to avoid unused-field warnings
        Objects.requireNonNull(cancellationRepository);
        Objects.requireNonNull(panicRepository);
        Objects.requireNonNull(activeRideRepository);
        Objects.requireNonNull(userRepository);
        Objects.requireNonNull(passengerRepository);
        Objects.requireNonNull(routeRepository);
        Objects.requireNonNull(driverRepository);
        Objects.requireNonNull(mapboxRoutingService);
        Objects.requireNonNull(webSocketController);
        Objects.requireNonNull(completedRideRepository);
        Objects.requireNonNull(emailService);
        Objects.requireNonNull(reportRepository);
        Objects.requireNonNull(panicNotifierService);
        Objects.requireNonNull(rideMapper);
        Objects.requireNonNull(notificationService);
        Objects.requireNonNull(ridePriceRepository);

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

    @AfterEach
    void tearDown() throws Exception {
        if (mocksCloser != null) mocksCloser.close();
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
        verify(driverRepository, times(1)).save(argThat(Driver::getActive));
    }

    @Test
    public void testFinishRide_ReportLinking_savesReports() {
        // Arrange
        when(activeRideRepository.findById(11L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(600L);
            return cr;
        });

        InconsistencyReport rep = new InconsistencyReport();
        rep.setId(300L);
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of(rep));

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(11L, req);

        // Assert
        assertNotNull(result);
        assertEquals(600L, result.getId());
        // verify that reportRepository.save was called for the report
        verify(reportRepository, times(1)).save(any(InconsistencyReport.class));
    }

    @Test
    public void testFinishRide_RepoSaveThrows_propagatesException() {
        when(activeRideRepository.findById(11L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenThrow(new RuntimeException("DB down"));

        UpdateRideDTO req = new UpdateRideDTO();

        assertThrows(RuntimeException.class, () -> rideService.finishRide(11L, req));

        verify(completedRideRepository, times(1)).save(any(CompletedRide.class));
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

    @Test
    public void testFinishRide_DriverHasNextScheduledRide_MarksDriverBusy() {
        // Arrange: driver has next scheduled ride
        when(activeRideRepository.findById(11L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(888L);
            return cr;
        });
        // simulate there is a scheduled ride for this driver
        ActiveRide next = new ActiveRide();
        when(activeRideRepository.findFirstByDriverAndStatusOrderByScheduledTimeAsc(eq(driver), eq(RideStatus.SCHEDULED))).thenReturn(Optional.of(next));

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(11L, req);

        // Assert
        assertNotNull(result);
        // when next ride exists driver should be set to active=false
        verify(driverRepository).save(argThat(d -> !d.getActive()));
    }

    @Test
    public void testFinishRide_SendsEmailToLinkedPassengers() {
        // Arrange
        Passenger linked = new Passenger();
        linked.setId(55L);
        linked.setEmail("linked@example.com");
        linked.setName("Lnk");
        activeRide.setLinkedPassengers(List.of(linked));

        when(activeRideRepository.findById(11L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(999L);
            return cr;
        });
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());
        when(activeRideRepository.findFirstByDriverAndStatusOrderByScheduledTimeAsc(any(Driver.class), any())).thenReturn(Optional.empty());

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(11L, req);

        // Assert
        assertNotNull(result);
        // emailService should be called for linked passenger as well
        verify(emailService).sendRideFinishedEmail(eq(linked.getEmail()), eq(linked.getName()), eq(result.getId()), eq(linked.getId()));
    }

}
