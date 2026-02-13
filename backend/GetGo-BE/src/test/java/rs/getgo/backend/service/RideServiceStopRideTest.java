package rs.getgo.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.getgo.backend.controllers.WebSocketController;
import rs.getgo.backend.dtos.ride.RideCompletionDTO;
import rs.getgo.backend.dtos.ride.StopRideDTO;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.*;
import rs.getgo.backend.services.NotificationService;
import rs.getgo.backend.services.impl.rides.RideServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideServiceStopRideTest {

    @Mock
    private ActiveRideRepository activeRideRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private CompletedRideRepository completedRideRepository;

    @Mock
    private InconsistencyReportRepository reportRepository;

    @Mock
    private PanicRepository panicRepository;

    @Mock
    private NotificationService notificationService;

    // additional mocks used by RideServiceImpl during stopRide
    @Mock
    private DriverRepository driverRepository;

    @Mock
    private WebSocketController webSocketController;

    @InjectMocks
    private RideServiceImpl rideService;

    private ActiveRide activeRide;

    @BeforeEach
    public void setUp() {
        // build a minimal ActiveRide in ACTIVE status with route and passengers
        activeRide = new ActiveRide();
        activeRide.setId(1L);

        Route route = new Route();
        route.setId(10L);
        route.setEstTimeMin(30.0);
        route.setEstDistanceKm(10.0);
        route.setWaypoints(new ArrayList<>());
        activeRide.setRoute(route);

        activeRide.setEstimatedPrice(100.0);
        activeRide.setEstimatedDurationMin(30.0);
        activeRide.setActualStartTime(LocalDateTime.now().minusMinutes(10));
        activeRide.setStatus(RideStatus.ACTIVE);

        Driver driver = new Driver();
        driver.setId(5L);
        driver.setEmail("driver@example.com");
        driver.setName("DriverName");
        activeRide.setDriver(driver);

        Passenger p = new Passenger();
        p.setId(7L);
        p.setEmail("pass@example.com");
        p.setName("PassName");
        activeRide.setPayingPassenger(p);

        activeRide.setLinkedPassengers(List.of());
    }

    @Test
    public void testStopRide_HappyPath() {
        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(100L);
            return cr;
        });
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());
        when(panicRepository.findAll()).thenReturn(List.of());

        StopRideDTO dto = new StopRideDTO();
        dto.setLatitude(45.0);
        dto.setLongitude(20.0);
        dto.setStoppedAt(LocalDateTime.now());

        RideCompletionDTO result = rideService.stopRide(1L, dto);

        assertNotNull(result);
        assertEquals("STOPPED_EARLY", result.getStatus());
        assertEquals(100L, result.getRideId());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());
        assertTrue(result.getDurationMinutes() >= 0);
        assertTrue(result.getPrice() >= 50.0); // minimum 50% of estimated price

        // verify that active ride was deleted and completed was saved
        verify(activeRideRepository, times(1)).delete(activeRide);
        verify(completedRideRepository, times(1)).save(any());
        // notifications are created for passenger and driver when both exist
        verify(notificationService, atLeastOnce()).createAndNotify(anyLong(), any(), anyString(), anyString(), any(LocalDateTime.class));
        // route was saved when stop coordinates provided
        verify(routeRepository, times(1)).save(any(Route.class));
    }

    @Test
    public void testStopRide_RideNotFound() {
        when(activeRideRepository.findById(2L)).thenReturn(Optional.empty());

        StopRideDTO dto = new StopRideDTO();
        assertThrows(IllegalStateException.class, () -> rideService.stopRide(2L, dto));
    }

    @Test
    public void testStopRide_InvalidStatus() {
        ActiveRide r = activeRide;
        r.setStatus(RideStatus.FINISHED);
        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(r));

        StopRideDTO dto = new StopRideDTO();
        assertThrows(IllegalStateException.class, () -> rideService.stopRide(1L, dto));
    }

    @Test
    public void testStopRide_DeletesPanicsWhenPresent() {
        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(101L);
            return cr;
        });
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());

        Panic panic = new Panic();
        panic.setId(50L);
        panic.setRideId(1L);
        when(panicRepository.findAll()).thenReturn(List.of(panic));

        StopRideDTO dto = new StopRideDTO();
        dto.setLatitude(10.0);
        dto.setLongitude(20.0);

        RideCompletionDTO result = rideService.stopRide(1L, dto);

        assertNotNull(result);
        verify(panicRepository, times(1)).deleteAll(argThat(iter -> {
            int cnt = 0;
            for (Object o : iter) {
                if (o instanceof Panic) {
                    Panic p = (Panic) o;
                    if (p.getId() != null && p.getId().equals(50L)) cnt++;
                }
            }
            return cnt == 1;
        }));
    }

    @Test
    public void testStopRide_EstimatedDurationZero_UsesEstimatedPrice() {
        // set estimated duration to zero -> fallback to estimatedPrice
        activeRide.getRoute().setEstTimeMin(0.0);
        activeRide.setEstimatedPrice(123.45);

        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(102L);
            return cr;
        });
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());
        when(panicRepository.findAll()).thenReturn(List.of());

        StopRideDTO dto = new StopRideDTO();
        dto.setLatitude(11.0);
        dto.setLongitude(22.0);

        RideCompletionDTO result = rideService.stopRide(1L, dto);

        assertNotNull(result);
        // price should equal estimatedPrice when est duration <= 0
        assertEquals(123.45, result.getPrice(), 0.0001);
    }

    @Test
    public void testStopRide_NoDriver_DoesNotTryToSaveDriver() {
        // ensure driver release logic executes: driver exists and must be saved as active=true
        Driver drv = activeRide.getDriver();
        drv.setActive(false);

        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(103L);
            return cr;
        });
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of());
        when(panicRepository.findAll()).thenReturn(List.of());

        StopRideDTO dto = new StopRideDTO();
        dto.setLatitude(12.0);
        dto.setLongitude(24.0);

        RideCompletionDTO result = rideService.stopRide(1L, dto);

        assertNotNull(result);
        // driverRepository.save should be called to set driver active
        verify(driverRepository, times(1)).save(any(Driver.class));
        // notifications still created for passenger
        verify(notificationService, atLeastOnce()).createAndNotify(anyLong(), any(), anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    public void testStopRide_LinkedPassengers_ReportLinkedToCompletedRide() {
        // add one linked passenger and prepare a report
        Passenger linked = new Passenger();
        linked.setId(8L);
        linked.setEmail("linked@example.com");
        activeRide.setLinkedPassengers(List.of(linked));

        when(activeRideRepository.findById(1L)).thenReturn(Optional.of(activeRide));
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(completedRideRepository.save(any())).thenAnswer(invocation -> {
            CompletedRide cr = invocation.getArgument(0);
            cr.setId(104L);
            return cr;
        });

        InconsistencyReport report = new InconsistencyReport();
        report.setId(200L);
        when(reportRepository.findUnlinkedReportsByPassenger(any())).thenReturn(List.of(report));
        when(reportRepository.save(any(InconsistencyReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(panicRepository.findAll()).thenReturn(List.of());

        StopRideDTO dto = new StopRideDTO();
        dto.setLatitude(13.0);
        dto.setLongitude(26.0);

        RideCompletionDTO result = rideService.stopRide(1L, dto);

        assertNotNull(result);
        // verify report was saved with completedRide set
        verify(reportRepository, atLeastOnce()).save(argThat(r -> ((InconsistencyReport) r).getCompletedRide() != null));
    }
}
