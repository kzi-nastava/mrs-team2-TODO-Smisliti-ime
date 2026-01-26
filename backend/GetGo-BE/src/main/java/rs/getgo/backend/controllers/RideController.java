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
import rs.getgo.backend.services.impl.rides.RideTrackingService;
import rs.getgo.backend.services.impl.rides.ScheduledRideService;
import rs.getgo.backend.utils.AuthUtils;

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
    private final ScheduledRideService scheduledRideService;

    public RideController(RideEstimateService rideEstimateService,
                          RideService rideService,
                          RideTrackingService rideTrackingService,
                          ScheduledRideService scheduledRideService) {
        this.rideEstimateService = rideEstimateService;
        this.rideService = rideService;
        this.rideTrackingService = rideTrackingService;
        this.scheduledRideService = scheduledRideService;
    }

    @PreAuthorize("hasRole('PASSENGER')")
    @GetMapping(value = "/passenger/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetPassengerActiveRideDTO> getPassengerActiveRide() {
        String email = AuthUtils.getCurrentUserEmail();
        GetPassengerActiveRideDTO ride = rideTrackingService.getPassengerActiveRide(email);
        if (ride == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(ride);
    }

    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping(value = "driver/all-scheduled", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GetActiveRideDTO>> getAllScheduledRides() {
        List<GetActiveRideDTO> rides = scheduledRideService.getScheduledRides();
        return ResponseEntity.ok(rides);
    }

    // 2.6.2 – Track ride
//    @PreAuthorize("hasRole('PASSENGER') or hasRole('DRIVER')")
    @GetMapping(value = "/{id}/tracking", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRideTrackingDTO> getRideTracking(@PathVariable("id") Long id) {
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
        UpdatedRideDTO updatedRide = rideService.finishRide(id, ride);

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

    // Driver cancels assigned ride before passengers enter
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/cancel/driver")
    public ResponseEntity<Void> cancelRideByDriver(@PathVariable Long rideId,
                                                   @RequestBody CancelRideRequestDTO body) {
        try {
            rideService.cancelRideByDriver(rideId, body.getReason());
            return ResponseEntity.ok().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Passenger cancels ride at least 10 minutes before start
    @PreAuthorize("hasRole('PASSENGER')")
    @PostMapping("/{rideId}/cancel/passenger")
    public ResponseEntity<Void> cancelRideByPassenger(@PathVariable Long rideId,
                                                      @RequestBody CancelRideRequestDTO body) {
        try {
            rideService.cancelRideByPassenger(rideId, body.getReason());
            return ResponseEntity.ok().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 2.6.5 – Stop ride while in progress
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping(value = "/{rideId}/stop", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RideCompletionDTO> stopRidePost(@PathVariable Long rideId, @RequestBody StopRideDTO stopRideDTO) throws Exception {
        RideCompletionDTO completion = rideService.stopRide(rideId, stopRideDTO);
        return ResponseEntity.ok(completion);
    }

    // 2.6.3 - PANIC button
    @PreAuthorize("hasRole('DRIVER') or hasRole('PASSENGER')")
    @PostMapping("/{rideId}/panic")
    public ResponseEntity<Void> createPanic(@PathVariable Long rideId) {
        String email = AuthUtils.getCurrentUserEmail();
        rideService.triggerPanic(rideId, email);
        return ResponseEntity.ok().build();
    }

    // 2.4.1 - Calling a ride
    @PreAuthorize("hasRole('PASSENGER')")
    @PostMapping("/order")
    public ResponseEntity<CreatedRideResponseDTO> orderRide(
            @RequestBody CreateRideRequestDTO request
    ) {
        String email = AuthUtils.getCurrentUserEmail();
        CreatedRideResponseDTO response = rideService.orderRide(request, email);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping(value = "/{rideId}/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedRideDTO> acceptRide(@PathVariable Long rideId) {
        UpdatedRideDTO response = rideService.acceptRide(rideId);
        return ResponseEntity.ok(response);
    }

    // 2.6.1 - Start ride
    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping(value = "/{rideId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedRideDTO> startRide(@PathVariable Long rideId) {
        UpdatedRideDTO response = rideService.startRide(rideId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping(value = "/driver/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetDriverActiveRideDTO> getDriverActiveRide() {
        String email = AuthUtils.getCurrentUserEmail();
        GetDriverActiveRideDTO ride = rideService.getDriverActiveRide(email);
        return ResponseEntity.ok(ride);
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
}