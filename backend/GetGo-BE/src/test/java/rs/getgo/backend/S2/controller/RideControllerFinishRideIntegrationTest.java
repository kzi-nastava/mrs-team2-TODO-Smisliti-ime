package rs.getgo.backend.S2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import rs.getgo.backend.dtos.ride.UpdateRideDTO;
import rs.getgo.backend.dtos.ride.UpdatedRideDTO;
import rs.getgo.backend.services.RideService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RideControllerFinishRideIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideService rideService;

    @Test
    public void finishRide_happyPath_returnsOkAndDto() throws Exception {
        UpdateRideDTO req = new UpdateRideDTO();
        req.setStatus("FINISHED");

        UpdatedRideDTO resp = new UpdatedRideDTO();
        resp.setId(123L);
        resp.setStatus("FINISHED");
        resp.setEndTime(LocalDateTime.now());

        when(rideService.finishRide(eq(1L), any(UpdateRideDTO.class))).thenReturn(resp);

        mockMvc.perform(put("/api/rides/1/finish")
                .with(SecurityMockMvcRequestPostProcessors.user("driver@x.com").roles("DRIVER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.status").value("FINISHED"));
    }

    @Test
    public void finishRide_serviceThrows_illegalState_leadsToBadRequest() throws Exception {
        UpdateRideDTO req = new UpdateRideDTO();
        req.setStatus("FINISHED");

        when(rideService.finishRide(eq(99L), any(UpdateRideDTO.class))).thenThrow(new IllegalStateException("Ride not found"));

        mockMvc.perform(put("/api/rides/99/finish")
                .with(SecurityMockMvcRequestPostProcessors.user("driver@x.com").roles("DRIVER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}

