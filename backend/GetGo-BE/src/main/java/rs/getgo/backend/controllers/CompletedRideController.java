package rs.getgo.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.getgo.backend.dtos.inconsistencyReport.GetInconsistencyReportDTO;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.services.CompletedRideService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("api/completed-rides")

public class CompletedRideController {

    private final CompletedRideService completedRideService;

    public CompletedRideController(CompletedRideService completedRideService) {
        this.completedRideService = completedRideService;
    }

    @GetMapping("/{rideId}/driver")
    public ResponseEntity<Long> getDriverId(@PathVariable Long rideId) {
        Long driverId = completedRideService.getDriverIdByRideId(rideId);
        return ResponseEntity.ok(driverId);
    }

//    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping(value = "/{rideId}/inconsistencies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetInconsistencyReportDTO>> getInconsistencyReport(@PathVariable Long rideId) throws Exception {
        List<GetInconsistencyReportDTO> reports = completedRideService.getInconsistencyReportsByRideId(rideId);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }

}
