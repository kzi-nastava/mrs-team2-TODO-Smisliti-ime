package rs.getgo.backend.S2.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
        activeRide.setId(1L);

        Route route = new Route();
        route.setId(2L);
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
        driver.setId(3L);
        driver.setEmail("drv@gmail.com");
        driver.setName("Drv");
        driver.setActive(false);
        activeRide.setDriver(driver);

        payingPassenger = new Passenger();
        payingPassenger.setId(4L);
        payingPassenger.setEmail("pass@gmail.com");
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
        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(5L);
            return cr;
        });
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());
        when(activeRideRepository.findFirstByDriverAndStatusOrderByScheduledTimeAsc(any(Driver.class), any())).thenReturn(Optional.empty());
        when(panicRepository.findByRideId(1L)).thenReturn(List.of());

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(1L, req);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("FINISHED", result.getStatus());
        assertNotNull(result.getEndTime());

        verify(completedRideRepository, times(1)).save(any(CompletedRide.class));
        verify(emailService, times(1)).sendRideFinishedEmail(eq(payingPassenger.getEmail()), eq(payingPassenger.getName()), eq(5L), eq(payingPassenger.getId()));
        verify(webSocketController, times(1)).notifyDriverRideFinished(eq(driver.getEmail()), eq(activeRide.getId()), anyDouble(), any(LocalDateTime.class), any(LocalDateTime.class), eq(driver.getId()));
        verify(webSocketController, times(1)).notifyPassengerRideFinished(eq(activeRide.getId()), anyDouble(), any(LocalDateTime.class), any(LocalDateTime.class), eq(driver.getId()));
        verify(activeRideRepository, times(1)).delete(activeRide);
        // driverRepository.save should be called to mark driver active (no scheduled rides => active true)
        verify(driverRepository, times(1)).save(argThat(Driver::getActive));
    }

    @Test
    public void testFinishRide_ReportLinking_savesReports() {
        // Arrange
        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(6L);
            return cr;
        });

        InconsistencyReport rep = new InconsistencyReport();
        rep.setId(7L);
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of(rep));

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(1L, req);

        // Assert
        assertNotNull(result);
        assertEquals(6L, result.getId());
        // verify that reportRepository.save was called for the report
        verify(reportRepository, times(1)).save(any(InconsistencyReport.class));
    }

    @Test
    public void testFinishRide_RepoSaveThrows_propagatesException() {
        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenThrow(new RuntimeException("DB down"));

        UpdateRideDTO req = new UpdateRideDTO();

        assertThrows(RuntimeException.class, () -> rideService.finishRide(1L, req));

        verify(completedRideRepository, times(1)).save(any(CompletedRide.class));
    }

    @Test
    public void testFinishRide_RideNotFound() {
        when(activeRideRepository.findById(10L)).thenReturn(Optional.empty());
        UpdateRideDTO req = new UpdateRideDTO();
        assertThrows(IllegalStateException.class, () -> rideService.finishRide(10L, req));
    }

    @Test
    public void testFinishRide_InvalidStatus_Throws() {
        activeRide.setStatus(RideStatus.DRIVER_READY);
        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        UpdateRideDTO req = new UpdateRideDTO();
        assertThrows(IllegalStateException.class, () -> rideService.finishRide(1L, req));
    }

    @Test
    public void testFinishRide_PanicsLinkedToCompletedRide() {
        // Arrange
        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(7L);
            return cr;
        });
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());

        Panic panic = new Panic();
        panic.setId(10L);
        // Return a single present Optional
        when(panicRepository.findByRideId(1L)).thenReturn(List.of(Optional.of(panic)));

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(1L, req);

        // Assert
        assertNotNull(result);
        assertEquals(7L, result.getId());
        // panic object should have been mutated to point to completed ride id
        assertEquals(7L, panic.getRideId());
        // completedRideRepository.save should have been called at least twice (initial save + update when panic attached)
        verify(completedRideRepository, atLeast(2)).save(any(CompletedRide.class));
    }

    @Test
    public void testFinishRide_DriverHasNextScheduledRide_MarksDriverBusy() {
        // Arrange: driver has next scheduled ride
        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(8L);
            return cr;
        });
        // simulate there is a scheduled ride for this driver
        ActiveRide next = new ActiveRide();
        when(activeRideRepository.findFirstByDriverAndStatusOrderByScheduledTimeAsc(eq(driver), eq(RideStatus.SCHEDULED))).thenReturn(Optional.of(next));

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(1L, req);

        // Assert
        assertNotNull(result);
        // when next ride exists driver should be set to active=false
        verify(driverRepository).save(argThat(d -> !d.getActive()));
    }

    @Test
    public void testFinishRide_SendsEmailToLinkedPassengers() {
        // Arrange
        Passenger linked = new Passenger();
        linked.setId(9L);
        linked.setEmail("linked@gmail.com");
        linked.setName("Lnk");
        activeRide.setLinkedPassengers(List.of(linked));

        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(9L);
            return cr;
        });
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());
        when(activeRideRepository.findFirstByDriverAndStatusOrderByScheduledTimeAsc(any(Driver.class), any())).thenReturn(Optional.empty());

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(1L, req);

        // Assert
        assertNotNull(result);
        // emailService should be called for linked passenger as well
        verify(emailService).sendRideFinishedEmail(eq(linked.getEmail()), eq(linked.getName()), eq(result.getId()), eq(linked.getId()));
    }

    @Test
    public void testFinishRide_ActivatesWaitingRide_NotifiesDriverAndPassenger() {
        // Arrange: create a waiting ride for the same driver
        ActiveRide waiting = new ActiveRide();
        waiting.setId(2L);
        waiting.setDriver(driver);
        waiting.setStatus(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE);

        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(10L);
            return cr;
        });
        // return a waiting ride that should be activated
        when(activeRideRepository.findByDriverAndStatus(eq(driver), eq(RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE))).thenReturn(Optional.of(waiting));
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());
        when(panicRepository.findByRideId(1L)).thenReturn(List.of());

        UpdateRideDTO req = new UpdateRideDTO();

        // Act
        UpdatedRideDTO result = rideService.finishRide(1L, req);

        // Assert
        assertNotNull(result);
        // waiting ride should be updated to DRIVER_READY and saved
        verify(activeRideRepository).save(argThat(ar -> ar.getId().equals(waiting.getId()) && ar.getStatus() == RideStatus.DRIVER_READY));

        // Verify web socket notifications for driver about next ride assigned
        verify(webSocketController).notifyDriverRideAssigned(eq(driver.getEmail()), any());
        // Verify passenger was notified about driver status update for waiting ride
        verify(webSocketController).notifyPassengerRideStatusUpdate(eq(waiting.getId()), eq(RideStatus.DRIVER_READY.toString()), anyString());
    }
}
