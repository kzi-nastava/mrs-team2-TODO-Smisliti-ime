package rs.getgo.backend.S3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import rs.getgo.backend.dtos.ride.RideCompletionDTO;
import rs.getgo.backend.dtos.ride.StopRideDTO;
import rs.getgo.backend.services.RideService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RideControllerStopRideIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideService rideService;

    @Test
    public void stopRide_happyPath_returnsOkAndDto() throws Exception {
        StopRideDTO req = new StopRideDTO();
        req.setLatitude(45.2671);
        req.setLongitude(19.8335);
        req.setStoppedAt(LocalDateTime.now());

        RideCompletionDTO resp = new RideCompletionDTO();
        resp.setRideId(123L);
        resp.setStatus("STOPPED_EARLY");
        resp.setPrice(60.0);

        when(rideService.stopRide(eq(1L), any(StopRideDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/rides/1/stop")
                .with(SecurityMockMvcRequestPostProcessors.user("driver@x.com").roles("DRIVER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STOPPED_EARLY"))
                .andExpect(jsonPath("$.rideId").value(123))
                .andExpect(jsonPath("$.price").value(60.0));
    }

    @Test
    public void stopRide_serviceThrows_illegalState_leadsTo5xx() throws Exception {
        StopRideDTO req = new StopRideDTO();
        req.setLatitude(45.0);
        req.setLongitude(20.0);

        when(rideService.stopRide(eq(99L), any(StopRideDTO.class))).thenThrow(new IllegalStateException("Ride not found"));

        mockMvc.perform(post("/api/rides/99/stop")
                .with(SecurityMockMvcRequestPostProcessors.user("driver@x.com").roles("DRIVER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
