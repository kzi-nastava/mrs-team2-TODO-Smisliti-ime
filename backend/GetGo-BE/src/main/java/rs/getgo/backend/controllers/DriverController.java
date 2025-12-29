package rs.getgo.backend.controllers;

import dtos.ride.GetRideDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    // 2.9.2
    @GetMapping(value = "/{id}/rides", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetRideDTO>> getDriverRides(
            @PathVariable("id") Long id,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate) {
        Collection<GetRideDTO> driverRides = new ArrayList<>() ;

        GetRideDTO ride1 = new GetRideDTO(1L, id, new ArrayList<>(), "Belgrade", "Novi Sad",
                LocalDateTime.of(2025, 12, 28, 14, 0),
                LocalDateTime.of(2025, 12, 28, 16, 0),
                120, false, true, "FINISHED", 25.50);

        GetRideDTO ride2 = new GetRideDTO(2L, id, new ArrayList<>(), "Belgrade", "Subotica",
                LocalDateTime.of(2025, 12, 29, 10, 0),
                LocalDateTime.of(2025, 12, 29, 13, 0),
                180, false, false, "ACTIVE", 35.00);

        if (startDate != null) {
            if (!ride1.getStartingTime().toLocalDate().isBefore(startDate)) driverRides.add(ride1);
            if (!ride2.getStartingTime().toLocalDate().isBefore(startDate)) driverRides.add(ride2);
        } else {
            driverRides.add(ride1);
            driverRides.add(ride2);
        }

        return new ResponseEntity<Collection<GetRideDTO>>(driverRides, HttpStatus.OK);
    }
}
