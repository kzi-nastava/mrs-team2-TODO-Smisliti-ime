package rs.getgo.backend.controllers;

import dtos.favorite.CreatedFavoriteDTO;
import dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import dtos.ride.*;
import dtos.rideEstimate.CreateRideEstimateDTO;
import dtos.rideEstimate.CreatedRideEstimateDTO;
import dtos.rideStatus.CreatedRideStatusDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    // 2.6.2 – Track ride
    @GetMapping(value = "/{id}/tracking", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRideTrackingDTO> trackRide(@PathVariable("id") Long id) {
        GetRideTrackingDTO ride = new GetRideTrackingDTO(id, 44.8176, 20.4569, 15.0);

        return new ResponseEntity<GetRideTrackingDTO>(ride, HttpStatus.OK);
    }

    // 2.6.2 – Create inconsistency report
    @PostMapping(value = "/{rideId}/inconsistencies", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedInconsistencyReportDTO> createInconsistencyReport(@RequestBody CreateInconsistencyReportDTO report, @PathVariable Long rideId) throws Exception {
        CreatedInconsistencyReportDTO savedInconsistencyReport = new CreatedInconsistencyReportDTO(1L, rideId, 501L, report.getText());


        return new ResponseEntity<CreatedInconsistencyReportDTO>(savedInconsistencyReport, HttpStatus.CREATED);
    }

    // 2.7 – Finish ride
    @PutMapping(value = "/{id}/finish", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedRideDTO> finishRide(@RequestBody UpdateRideDTO ride, @PathVariable Long id)
            throws Exception {
        UpdatedRideDTO updatedRide = new UpdatedRideDTO(id, ride.getStatus(), java.time.LocalDateTime.now());

        return new ResponseEntity<UpdatedRideDTO>(updatedRide, HttpStatus.OK);
    }

    // 2.1.2 – Create ride estimate
    @PostMapping("/estimate")
    public ResponseEntity<CreatedRideEstimateDTO> createRideEstimate(
            @RequestBody CreateRideEstimateDTO request) {

        CreatedRideEstimateDTO response = new CreatedRideEstimateDTO(850.0, 15, 6.3);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.5 – Cancel ride
    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<CreatedRideStatusDTO> cancelRide(@PathVariable Long rideId) {
        CreatedRideStatusDTO response = new CreatedRideStatusDTO(rideId, "CANCELED");
        return ResponseEntity.ok(response);
    }

    // 2.6.5 – Stop ride
    @PutMapping("/{rideId}/stop")
    public ResponseEntity<CreatedRideStatusDTO> stopRide(@PathVariable Long rideId) {
        CreatedRideStatusDTO response = new CreatedRideStatusDTO(rideId, "FINISHED");
        return ResponseEntity.ok(response);
    }

    // 2.6.1 - Start ride
    @PutMapping(value = "/{rideId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedRideDTO> startRide(@PathVariable Long rideId) {
        UpdatedRideDTO response = new UpdatedRideDTO();
        response.setId(rideId);
        return ResponseEntity.ok(response);
    }

    // 2.4.3 - Calling favorite ride
    @GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GetRideDTO>> getFavoriteRides() {
        List<GetRideDTO> response = new ArrayList<>();
        return ResponseEntity.ok(response);
    }

    // 2.4.3 - Calling favorite ride
    @PostMapping(value = "/{rideId}/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedFavoriteDTO> favoriteRide(@PathVariable Long rideId) {
        CreatedFavoriteDTO response = new CreatedFavoriteDTO();
        response.setRideId(rideId);
        response.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.4.3 - Calling favorite ride
    @DeleteMapping("/{rideId}/favorite")
    public ResponseEntity<Void> unfavoriteRide(@PathVariable Long rideId) {
        return ResponseEntity.noContent().build();
    }

    // 2.4.1 - Calling a ride
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedRideDTO> createRide(@RequestBody CreateRideDTO request) {

        CreatedRideDTO response = new CreatedRideDTO();

        if (request.getScheduledTime() == null) {
            // Immediate ride
            response.setRideId(1L);
            response.setStatus("ACCEPTED");
            response.setDriverId(5L);
        } else {
            // Scheduled ride
            response.setRideId(2L);
            response.setStatus("SCHEDULED");
            response.setScheduledTime(request.getScheduledTime());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}