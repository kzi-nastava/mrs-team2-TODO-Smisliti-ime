package rs.getgo.backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import rs.getgo.backend.dtos.authentication.GetActivationTokenDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.CreatedDriverChangeRequestDTO;
import rs.getgo.backend.dtos.driver.GetActiveDriverLocationDTO;
import rs.getgo.backend.dtos.driver.GetDriverDTO;
import rs.getgo.backend.dtos.driver.UpdateDriverDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    // 2.2.3 - Driver registration
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/activate/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetActivationTokenDTO> validateActivationToken(
            @PathVariable String token) {

        GetActivationTokenDTO response = new GetActivationTokenDTO();
        response.setValid(true);
        response.setEmail("driver@example.com");

        return ResponseEntity.ok(response);
    }

    // 2.2.3 - Driver registration
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/activate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPasswordDTO> setDriverPassword(
            @RequestBody UpdatePasswordDTO request) {

        UpdatedPasswordDTO response = new UpdatedPasswordDTO();
        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetDriverDTO> getProfile() {

        GetDriverDTO response = new GetDriverDTO();
        response.setId(2L);
        response.setEmail("driver@example.com");
        response.setName("Driver");
        response.setRecentHoursWorked(6);

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PutMapping(value = "/profile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> updateProfile(
            @RequestBody UpdateDriverDTO request) {

        CreatedDriverChangeRequestDTO response = new CreatedDriverChangeRequestDTO();
        response.setRequestId(1L);
        response.setStatus("PENDING");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile
    @PostMapping(value = "/profile/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {

        CreatedDriverChangeRequestDTO response = new CreatedDriverChangeRequestDTO();
        response.setRequestId(2L);
        response.setStatus("PENDING");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.4.1 - Calling a ride
    @PreAuthorize("hasRole('PASSENGER')")
    @GetMapping(value = "/active-locations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GetActiveDriverLocationDTO>> getActiveDriverLocations() {

        List<GetActiveDriverLocationDTO> response = new ArrayList<>();
        GetActiveDriverLocationDTO driver = new GetActiveDriverLocationDTO();
        driver.setDriverId(2L);
        driver.setLatitude(45.2550);
        driver.setVehicleType("STANDARD");
        response.add(driver);

        return ResponseEntity.ok(response);
    }
}