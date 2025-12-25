package controllers;

import dtos.responses.RideEstimateResponseDTO;
import dtos.responses.RideStatusResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}