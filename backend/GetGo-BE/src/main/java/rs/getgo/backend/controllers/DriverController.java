package rs.getgo.backend.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import rs.getgo.backend.dtos.authentication.GetActivationTokenDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.*;
import rs.getgo.backend.dtos.request.CreatedDriverChangeRequestDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.getgo.backend.services.DriverServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    @Autowired
    private DriverServiceImpl driverService;

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
    @GetMapping(value = "/activate/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetActivationTokenDTO> validateActivationToken(
            @PathVariable String token) {

        GetActivationTokenDTO response = new GetActivationTokenDTO();
        response.setValid(true);
        response.setEmail("driver@example.com");

        return ResponseEntity.ok(response);
    }

    // 2.2.3 - Driver registration
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
        Long driverId = 1L; // TODO: get from cookie/whatever we decide to use
        GetDriverDTO response = driverService.getDriverById(driverId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (Request personal info change)
    @PutMapping(value = "/profile/personal",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> updatePersonalInfo(
            @RequestBody UpdateDriverPersonalDTO updateDriverPersonalDTO) {
        Long driverId = 1L; // TODO: get from cookie/whatever we decide to use
        CreatedDriverChangeRequestDTO response = driverService.requestPersonalInfoChange(driverId, updateDriverPersonalDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile (Request vehicle info change)
    @PutMapping(value = "/profile/vehicle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> updateVehicleInfo(
            @RequestBody UpdateDriverVehicleDTO updateDriverVehicleDTO) {
        Long driverId = 1L; // TODO: get from cookie/whatever we decide to use
        CreatedDriverChangeRequestDTO response = driverService.requestVehicleInfoChange(driverId, updateDriverVehicleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile (Request profile picture change)
    @PostMapping(value = "/profile/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {
        Long driverId = 1L; // TODO: get from cookie/whatever we decide to use
        CreatedDriverChangeRequestDTO response = driverService.requestProfilePictureChange(driverId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile (Change driver password)
    @PutMapping(value = "/profile/password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPasswordDTO> updatePassword(
            @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        Long driverId = 1L; // TODO: get from cookie/whatever we decide to use
        UpdatedPasswordDTO response = driverService.updatePassword(driverId, updatePasswordDTO);
        if (!response.getSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // 2.4.1 - Calling a ride
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
