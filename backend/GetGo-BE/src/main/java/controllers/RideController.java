package controllers;

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

    // 2.6.2 During the ride
    @GetMapping(value = "/{id}/tracking", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRideTrackingDTO> trackRide(@PathVariable("id") Long id) {
        GetRideTrackingDTO ride = new GetRideTrackingDTO();

        return new ResponseEntity<GetRideTrackingDTO>(ride, HttpStatus.OK);
    }

    // 2.6.2 During the ride
    @PostMapping(value = "/{rideId}/inconsistencies", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedInconsistencyReportDTO> createInconsistencyReport(@RequestBody CreateInconsistencyReportDTO report, @PathVariable Long rideId) throws Exception {
        CreatedInconsistencyReportDTO savedInconsistencyReport = new CreatedInconsistencyReportDTO();

        return new ResponseEntity<CreatedInconsistencyReportDTO>(savedInconsistencyReport, HttpStatus.CREATED);
    }

    // 2.7 Ride Completion
    @PutMapping(value = "/{id}/finish", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedRideDTO> finishRide(@RequestBody UpdateRideDTO ride, @PathVariable Long id)
            throws Exception {
        UpdatedRideDTO updatedRide = new UpdatedRideDTO();

        return new ResponseEntity<UpdatedRideDTO>(updatedRide, HttpStatus.OK);
    }

    @PostMapping("/estimate")
    public ResponseEntity<CreatedRideEstimateDTO> createRideEstimate(
            @RequestBody CreateRideEstimateDTO request) {

        CreatedRideEstimateDTO response = new CreatedRideEstimateDTO(850.0, 15, 6.3);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<CreatedRideStatusDTO> cancelRide(@PathVariable Long rideId) {
        CreatedRideStatusDTO response = new CreatedRideStatusDTO(rideId, "CANCELED");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{rideId}/stop")
    public ResponseEntity<CreatedRideStatusDTO> stopRide(@PathVariable Long rideId) {
        CreatedRideStatusDTO response = new CreatedRideStatusDTO(rideId, "FINISHED");
        return ResponseEntity.ok(response);
    }
}