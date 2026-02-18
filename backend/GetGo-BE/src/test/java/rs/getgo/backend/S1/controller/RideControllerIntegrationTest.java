package rs.getgo.backend.S1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.getgo.backend.dtos.ride.CreateRideRequestDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.services.impl.rides.MapboxRoutingService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(value = "/sql/S1/integration/order-ride-integration-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RideControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActiveRideRepository activeRideRepository;

    @SpyBean
    private MapboxRoutingService routingService;

    @BeforeEach
    void setupMapboxMock() {
        MapboxRoutingService.RouteResponse mockResponse = new MapboxRoutingService.RouteResponse(
                List.of(
                        new MapboxRoutingService.Coordinate(45.252814, 19.847549),
                        new MapboxRoutingService.Coordinate(45.241073, 19.821839)
                ),
                300.0,
                5,
                5.0
        );

        doReturn(mockResponse)
                .when(routingService)
                .getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    @WithMockUser(username = "p@gmail.com", roles = {"PASSENGER"})
    void should_orderRideSuccessfully_when_allValid() throws Exception {
        CreateRideRequestDTO request = new CreateRideRequestDTO();
        request.setLatitudes(List.of(45.252814, 45.241073));
        request.setLongitudes(List.of(19.847549, 19.821839));
        request.setAddresses(List.of("Zarka Zrenjanina 5", "Bulevar Evrope 4-57"));
        request.setHasBaby(false);
        request.setHasPets(false);
        request.setVehicleType(null);

        mockMvc.perform(post("/api/rides/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.rideId").isNumber());

        List<ActiveRide> rides = activeRideRepository.findAll();
        ActiveRide createdRide = rides.stream()
                .filter(r -> r.getPayingPassenger().getEmail().equals("p@gmail.com"))
                .filter(r -> r.getStatus() == RideStatus.DRIVER_READY
                        || r.getStatus() == RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE)
                .findFirst()
                .orElse(null);

        assertNotNull(createdRide, "Ride not be null");
        assertNotNull(createdRide.getDriver(), "Driver should not be null");
        assertNotNull(createdRide.getRoute(), "Route should not be null");
        assertEquals("Zarka Zrenjanina 5", createdRide.getRoute().getStartingPoint());
        assertEquals("Bulevar Evrope 4-57", createdRide.getRoute().getEndingPoint());
        assertEquals(2, createdRide.getRoute().getWaypoints().size());
        assertFalse(createdRide.isNeedsBabySeats());
        assertFalse(createdRide.isNeedsPetFriendly());
    }
}