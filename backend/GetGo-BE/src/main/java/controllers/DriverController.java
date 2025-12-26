package controllers;

import dtos.ride.GetRideDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    // 2.9.2
    @GetMapping(value = "/{id}/rides", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetRideDTO>> getDriverRides(@PathVariable("id") Long id, @RequestParam(required = false) String startDate) {
        Collection<GetRideDTO> driverRides = new ArrayList<>() ;

        return new ResponseEntity<Collection<GetRideDTO>>(driverRides, HttpStatus.OK);
    }
}
