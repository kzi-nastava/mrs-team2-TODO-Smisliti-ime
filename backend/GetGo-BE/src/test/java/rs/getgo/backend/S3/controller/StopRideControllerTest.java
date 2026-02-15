package rs.getgo.backend.S3.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.getgo.backend.controllers.RideController;
import rs.getgo.backend.dtos.ride.RideCompletionDTO;
import rs.getgo.backend.dtos.ride.StopRideDTO;
import rs.getgo.backend.services.RideEstimateService;
import rs.getgo.backend.services.RideOrderService;
import rs.getgo.backend.services.RideService;
import rs.getgo.backend.services.impl.rides.FavoriteRideService;
import rs.getgo.backend.services.impl.rides.RideTrackingService;
import rs.getgo.backend.services.impl.rides.ScheduledRideService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class StopRideControllerTest {

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
        MockitoAnnotations.openMocks(this);
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
    void stopRide_delegatesToService_and_returnsOk() throws Exception {
        // Arrange
        StopRideDTO req = new StopRideDTO();
        req.setLatitude(45.0);
        req.setLongitude(20.0);

        RideCompletionDTO dto = new RideCompletionDTO();
        dto.setRideId(321L);
        dto.setStatus("STOPPED_EARLY");
        dto.setPrice(55.0);

        when(rideService.stopRide(anyLong(), any(StopRideDTO.class))).thenReturn(dto);

        // Act
        var response = controller.stopRidePost(1L, req);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(321L, response.getBody().getRideId());
        verify(rideService, times(1)).stopRide(1L, req);
    }
}

