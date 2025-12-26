package controllers;

import dtos.responses.RideEstimateResponseDTO;
import dtos.responses.RideStatusResponseDTO;
import dtos.ride.GetRideDTO;
import dtos.ride.UpdateRideDTO;
import dtos.ride.UpdatedRideDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    // 2.1.2 Ride Estimation
    @GetMapping("/estimate")
    public ResponseEntity<RideEstimateResponseDTO> estimateRide(
            @RequestParam double startLat,
            @RequestParam double startLng,
            @RequestParam double endLat,
            @RequestParam double endLng,
            @RequestParam String vehicleType) {

        RideEstimateResponseDTO response =
                new RideEstimateResponseDTO(850.0, 15, 6.3);

        return ResponseEntity.ok(response);
    }

    // 2.5 Ride Cancellation
    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<RideStatusResponseDTO> cancelRide(
            @PathVariable Long rideId) {

        RideStatusResponseDTO response =
                new RideStatusResponseDTO(rideId, "CANCELED");

        return ResponseEntity.ok(response);
    }

    // 2.6.5 Ride Stopping
    @PutMapping("/{rideId}/stop")
    public ResponseEntity<RideStatusResponseDTO> stopRide(
            @PathVariable Long rideId) {

        RideStatusResponseDTO response =
                new RideStatusResponseDTO(rideId, "FINISHED");

        return ResponseEntity.ok(response);
    }

    // 2.6.2
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRideDTO> trackRide(@PathVariable("id") Long id) {
        GetRideDTO ride = new GetRideDTO();

        return new ResponseEntity<GetRideDTO>(ride, HttpStatus.OK);
    }

    // 2.7
    @PutMapping(value = "/{id}/finish", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedRideDTO> finishRide(@RequestBody UpdateRideDTO ride, @PathVariable Long id)
            throws Exception {
        UpdatedRideDTO updatedRide = new UpdatedRideDTO();

        return new ResponseEntity<UpdatedRideDTO>(updatedRide, HttpStatus.OK);
    }
}