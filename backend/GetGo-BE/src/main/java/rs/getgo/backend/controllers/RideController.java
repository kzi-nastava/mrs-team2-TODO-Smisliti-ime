package rs.getgo.backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import rs.getgo.backend.dtos.favorite.CreatedFavoriteDTO;
import rs.getgo.backend.dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import rs.getgo.backend.dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import rs.getgo.backend.dtos.ride.*;
import rs.getgo.backend.dtos.rideEstimate.CreateRideEstimateDTO;
import rs.getgo.backend.dtos.rideEstimate.CreatedRideEstimateDTO;
import rs.getgo.backend.dtos.rideStatus.CreatedRideStatusDTO;
import rs.getgo.backend.services.RideEstimateService;
import rs.getgo.backend.services.RideService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.services.RideTrackingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
@CrossOrigin(origins = "http://localhost:4200")
public class RideController {

    private final RideEstimateService rideEstimateService;
    private final RideService rideService;
    private final RideTrackingService rideTrackingService;

    public RideController(RideEstimateService rideEstimateService, RideService rideService, RideTrackingService rideTrackingService) {
        this.rideEstimateService = rideEstimateService;
        this.rideService = rideService;
        this.rideTrackingService = rideTrackingService;
    }

    // 2.6.2 – Track ride
//    @PreAuthorize("hasRole('PASSENGER') or hasRole('DRIVER')")
    @GetMapping(value = "/{id}/tracking", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRideTrackingDTO> trackRide(@PathVariable("id") Long id) {
        GetRideTrackingDTO ride = rideTrackingService.getRideTracking(id);

        return ResponseEntity.ok(ride);
    }

    // 2.6.2 – Create inconsistency report
//    @PreAuthorize("hasRole('PASSENGER') or hasRole('DRIVER') or hasRole('ADMIN')")
    @PostMapping(value = "/{rideId}/inconsistencies", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedInconsistencyReportDTO> createInconsistencyReport(@RequestBody CreateInconsistencyReportDTO report, @PathVariable Long rideId) throws Exception {
//        CreatedInconsistencyReportDTO savedInconsistencyReport = new CreatedInconsistencyReportDTO(1L, rideId, 501L, report.getText());

        CreatedInconsistencyReportDTO savedReportDTO = rideTrackingService.saveInconsistencyReport(rideId, report);


//        return new ResponseEntity<CreatedInconsistencyReportDTO>(savedInconsistencyReport, HttpStatus.CREATED);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReportDTO);
    }

    // 2.7 – Finish ride
    @PreAuthorize("hasRole('DRIVER')")
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

        // delegate to service which geocodes, estimates distance/time and returns DTO
        CreatedRideEstimateDTO response = rideEstimateService.createEstimate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.5 – Cancel ride
    @PreAuthorize("hasRole('PASSENGER')")
    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<CreatedRideStatusDTO> cancelRide(@PathVariable Long rideId,
                                                           @RequestBody CancelRideDTO cancelRequest) {
        try {
            CreatedRideStatusDTO response = rideService.cancelRide(rideId, cancelRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 2.6.5 – Stop ride
    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping("/{rideId}/stop")
    public ResponseEntity<CreatedRideStatusDTO> stopRide(@PathVariable Long rideId) {
        CreatedRideStatusDTO response = new CreatedRideStatusDTO(rideId, "FINISHED");
        return ResponseEntity.ok(response);
    }

    // 2.6.3 - PANIC button
    @PreAuthorize("hasRole('DRIVER') or hasRole('PASSENGER')")
    @PostMapping("/{rideId}/panic")
    public ResponseEntity<Void> createPanic(
            @PathVariable Long rideId,
            @PathVariable String email
    ) {
        rideService.triggerPanic(rideId, email);
        return ResponseEntity.ok().build();
    }

    // 2.6.1 - Start ride
    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping(value = "/{rideId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedRideDTO> startRide(@PathVariable Long rideId) {
        UpdatedRideDTO response = new UpdatedRideDTO();
        response.setId(rideId);
        return ResponseEntity.ok(response);
    }

    // 2.4.3 - Calling favorite ride
    @PreAuthorize("hasRole('PASSENGER')")
    @GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GetRideDTO>> getFavoriteRides() {
        List<GetRideDTO> response = new ArrayList<>();
        return ResponseEntity.ok(response);
    }

    // 2.4.3 - Calling favorite ride
    @PreAuthorize("hasRole('PASSENGER')")
    @PostMapping(value = "/{rideId}/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedFavoriteDTO> favoriteRide(@PathVariable Long rideId) {
        CreatedFavoriteDTO response = new CreatedFavoriteDTO();
        response.setRideId(rideId);
        response.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.4.3 - Calling favorite ride
    @PreAuthorize("hasRole('PASSENGER')")
    @DeleteMapping("/{rideId}/favorite")
    public ResponseEntity<Void> unfavoriteRide(@PathVariable Long rideId) {
        return ResponseEntity.noContent().build();
    }

    // 2.4.1 - Calling a ride
    @PreAuthorize("hasRole('PASSENGER')")
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