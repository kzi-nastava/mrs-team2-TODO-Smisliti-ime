package rs.getgo.backend.S2.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.getgo.backend.controllers.RideController;
import rs.getgo.backend.dtos.ride.UpdateRideDTO;
import rs.getgo.backend.dtos.ride.UpdatedRideDTO;
import rs.getgo.backend.services.RideEstimateService;
import rs.getgo.backend.services.RideOrderService;
import rs.getgo.backend.services.RideService;
import rs.getgo.backend.services.impl.rides.FavoriteRideService;
import rs.getgo.backend.services.impl.rides.RideTrackingService;
import rs.getgo.backend.services.impl.rides.ScheduledRideService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


class FinishRideControllerTest {

    @Mock
    private RideEstimateService rideEstimateService;

    @Mock
    private RideService rideService;

    @Mock
    private RideOrderService rideOrderService;

    @Mock
    private RideTrackingService rideTrackingService;

    @Mock
    private ScheduledRideService scheduledRideService;

    @Mock
    private FavoriteRideService favoriteRideService;

    private RideController controller;

    @BeforeEach
    void initMocks() {
        // initialize @Mock annotated fields without using MockitoExtension and without holding AutoCloseable
        MockitoAnnotations.openMocks(this);

        // construct controller with mocked dependencies
        controller = new RideController(
                rideEstimateService,
                rideService,
                rideOrderService,
                rideTrackingService,
                scheduledRideService,
                favoriteRideService
        );
    }

    @Test
    void finishRide_delegatesToService_and_returnsOk() throws Exception {
        // Arrange
        UpdateRideDTO req = new UpdateRideDTO();
        UpdatedRideDTO dto = new UpdatedRideDTO();
        dto.setId(123L);
        dto.setStatus("FINISHED");
        dto.setEndTime(LocalDateTime.now());

        when(rideService.finishRide(anyLong(), any(UpdateRideDTO.class))).thenReturn(dto);

        // Act
        var response = controller.finishRide(req, 11L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(123L, response.getBody().getId());
        verify(rideService, times(1)).finishRide(11L, req);
    }
}
