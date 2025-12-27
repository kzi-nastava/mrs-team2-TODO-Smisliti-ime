package controllers;

import dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inconsistency-reports")
public class InconsistencyReportController {
    // 2.6.2  prebacio sam u rideController
//    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<CreatedInconsistencyReportDTO> createInconsistencyReport(@RequestBody CreateInconsistencyReportDTO report) throws Exception {
//        CreatedInconsistencyReportDTO savedInconsistencyReport = new CreatedInconsistencyReportDTO();
//
//        return new ResponseEntity<CreatedInconsistencyReportDTO>(savedInconsistencyReport, HttpStatus.CREATED);
//    }
}
