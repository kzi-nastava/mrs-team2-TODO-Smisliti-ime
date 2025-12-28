package rs.getgo.backend.controllers;

import dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import dtos.ride.GetRideTrackingDTO;
import dtos.ride.UpdateRideDTO;
import dtos.ride.UpdatedRideDTO;
import dtos.rideEstimate.CreateRideEstimateDTO;
import dtos.rideEstimate.CreatedRideEstimateDTO;
import dtos.rideStatus.CreatedRideStatusDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    // 2.6.2 – Track ride
    @GetMapping(value = "/{id}/tracking", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRideTrackingDTO> trackRide(@PathVariable("id") Long id) {
        GetRideTrackingDTO ride = new GetRideTrackingDTO();

        return new ResponseEntity<GetRideTrackingDTO>(ride, HttpStatus.OK);
    }

    // 2.6.2 – Create inconsistency report
    @PostMapping(value = "/{rideId}/inconsistencies", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedInconsistencyReportDTO> createInconsistencyReport(@RequestBody CreateInconsistencyReportDTO report, @PathVariable Long rideId) throws Exception {
        CreatedInconsistencyReportDTO savedInconsistencyReport = new CreatedInconsistencyReportDTO();

        return new ResponseEntity<CreatedInconsistencyReportDTO>(savedInconsistencyReport, HttpStatus.CREATED);
    }

    // 2.7 – Finish ride
    @PutMapping(value = "/{id}/finish", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedRideDTO> finishRide(@RequestBody UpdateRideDTO ride, @PathVariable Long id)
            throws Exception {
        UpdatedRideDTO updatedRide = new UpdatedRideDTO();

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
}