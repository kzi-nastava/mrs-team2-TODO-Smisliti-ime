package rs.getgo.backend.S1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.getgo.backend.controllers.RideController;
import rs.getgo.backend.dtos.ride.CreateRideRequestDTO;
import rs.getgo.backend.dtos.ride.CreatedRideResponseDTO;
import rs.getgo.backend.services.RideEstimateService;
import rs.getgo.backend.services.RideOrderService;
import rs.getgo.backend.services.RideService;
import rs.getgo.backend.services.impl.rides.FavoriteRideService;
import rs.getgo.backend.services.impl.rides.RideTrackingService;
import rs.getgo.backend.services.impl.rides.ScheduledRideService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = RideController.class)
@EnableMethodSecurity
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideEstimateService rideEstimateService;

    @MockBean
    private RideService rideService;

    @MockBean
    private RideOrderService rideOrderService;

    @MockBean
    private RideTrackingService rideTrackingService;

    @MockBean
    private ScheduledRideService scheduledRideService;

    @MockBean
    private FavoriteRideService favoriteRideService;

    private CreateRideRequestDTO makeValidRideRequest() {
        CreateRideRequestDTO request = new CreateRideRequestDTO();
        request.setLatitudes(List.of(45.252814, 45.241073));
        request.setLongitudes(List.of(19.847549, 19.821839));
        request.setAddresses(List.of("Zarka Zrenjanina 5", "Bulevar Evrope 4-57"));
        request.setHasBaby(false);
        request.setHasPets(false);
        request.setVehicleType(null);

        return request;
    }

    @Test
    @WithMockUser(username = "p@gmail.com", roles = {"PASSENGER"})
    public void should_returnOk_when_rideValid() throws Exception {
        CreateRideRequestDTO request = makeValidRideRequest();

        CreatedRideResponseDTO expectedResponse = new CreatedRideResponseDTO();
        expectedResponse.setStatus("SUCCESS");
        expectedResponse.setMessage("Ride ordered successfully");
        expectedResponse.setRideId(1L);

        Mockito.when(rideOrderService.orderRide(any(CreateRideRequestDTO.class), eq("p@gmail.com")))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/rides/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Ride ordered successfully"))
                .andExpect(jsonPath("$.rideId").value(1L));
    }

    @Test
    @WithMockUser(username = "p@gmail.com", roles = {"PASSENGER"})
    public void should_returnOk_when_complexRideValid() throws Exception {
        CreateRideRequestDTO request = new CreateRideRequestDTO();
        request.setLatitudes(List.of(45.252814, 45.241073, 45.244243, 45.263932));
        request.setLongitudes(List.of(19.847549, 19.821839, 19.825209, 19.824880));
        request.setAddresses(List.of("Zarka Zrenjanina 5", "Bulevar Evrope 4-57", "9- 11B- 70", "Bulevar Jase Tomica 5"));
        request.setHasBaby(true);
        request.setHasPets(true);
        request.setVehicleType("STANDARD");
        request.setScheduledTime("12:54");
        request.setFriendEmails(List.of("o@gmail.com", "i@gmail.com", "u@gmail.com"));
        CreatedRideResponseDTO expectedResponse = new CreatedRideResponseDTO();
        expectedResponse.setStatus("SUCCESS");
        expectedResponse.setMessage("Ride ordered successfully");
        expectedResponse.setRideId(1L);

        Mockito.when(rideOrderService.orderRide(any(CreateRideRequestDTO.class), eq("p@gmail.com")))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/rides/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Ride ordered successfully"))
                .andExpect(jsonPath("$.rideId").value(1L));
    }

    @Test
    @WithMockUser(username = "driver@gmail.com", roles = {"DRIVER"})
    public void should_returnForbidden_when_invalidRole() throws Exception {
        CreateRideRequestDTO request = makeValidRideRequest();

        CreatedRideResponseDTO expectedResponse = new CreatedRideResponseDTO();
        expectedResponse.setStatus("SUCCESS");
        expectedResponse.setMessage("Ride ordered successfully");
        expectedResponse.setRideId(1L);

        mockMvc.perform(post("/api/rides/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void should_returnUnauthorized_when_unauthenticated() throws Exception {
        CreateRideRequestDTO request = makeValidRideRequest();

        CreatedRideResponseDTO expectedResponse = new CreatedRideResponseDTO();
        expectedResponse.setStatus("SUCCESS");
        expectedResponse.setMessage("Ride ordered successfully");
        expectedResponse.setRideId(1L);

        mockMvc.perform(post("/api/rides/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "p@gmail.com", roles = {"PASSENGER"})
    public void should_returnBadRequest_when_latitudesNull() throws Exception {
        CreateRideRequestDTO request = new CreateRideRequestDTO();
        request.setLatitudes(null);
        request.setLongitudes(List.of(19.847549, 19.821839));
        request.setAddresses(List.of("Zarka Zrenjanina 5", "Bulevar Evrope 4-57"));
        request.setHasBaby(false);
        request.setHasPets(false);
        request.setVehicleType("STANDARD");

        mockMvc.perform(post("/api/rides/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "p@gmail.com", roles = {"PASSENGER"})
    public void should_returnBadRequest_when_latitudesLessThanTwo() throws Exception {
        CreateRideRequestDTO request = new CreateRideRequestDTO();
        request.setLatitudes(List.of(45.252814));
        request.setLongitudes(List.of(19.847549, 19.821839));
        request.setAddresses(List.of("Zarka Zrenjanina 5", "Bulevar Evrope 4-57"));
        request.setHasBaby(false);
        request.setHasPets(false);
        request.setVehicleType("STANDARD");

        mockMvc.perform(post("/api/rides/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "p@gmail.com", roles = {"PASSENGER"})
    public void should_returnBadRequest_when_scheduledTimeInvalid() throws Exception {
        CreateRideRequestDTO request = new CreateRideRequestDTO();
        request.setLatitudes(List.of(45.252814, 45.241073));
        request.setLongitudes(List.of(19.847549, 19.821839));
        request.setAddresses(List.of("Zarka Zrenjanina 5", "Bulevar Evrope 4-57"));
        request.setScheduledTime("30:60");
        request.setHasBaby(false);
        request.setHasPets(false);
        request.setVehicleType("STANDARD");

        mockMvc.perform(post("/api/rides/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "p@gmail.com", roles = {"PASSENGER"})
    public void should_returnBadRequest_when_friendEmailsInvalid() throws Exception {
        CreateRideRequestDTO request = new CreateRideRequestDTO();
        request.setLatitudes(List.of(45.252814, 45.241073));
        request.setLongitudes(List.of(19.847549, 19.821839));
        request.setAddresses(List.of("Zarka Zrenjanina 5", "Bulevar Evrope 4-57"));
        request.setFriendEmails(List.of("valid@gmail.com", "notanemail", "another@gmail.com"));
        request.setHasBaby(false);
        request.setHasPets(false);
        request.setVehicleType("STANDARD");

        mockMvc.perform(post("/api/rides/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}