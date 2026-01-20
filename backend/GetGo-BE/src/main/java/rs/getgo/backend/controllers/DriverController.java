package rs.getgo.backend.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import rs.getgo.backend.dtos.authentication.GetActivationTokenDTO;
import rs.getgo.backend.dtos.authentication.UpdateDriverPasswordDTO;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/drivers")
public class DriverController {

    @Autowired
    private DriverServiceImpl driverService;

    Long driverId = 6L; // TODO: get from cookie/whatever we decide to use
    String driverEmail = "d@gmail.com"; // TODO: get from cookie/whatever we decide to use

    public DriverController(DriverServiceImpl driverService) {
        this.driverService = driverService;
    }

    // 2.9.2
    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping(value = "/{id}/rides", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetRideDTO>> getDriverRides(
            @PathVariable("id") Long id,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate) {

        List<GetRideDTO> rides = driverService.getDriverRides(id, startDate);
        return ResponseEntity.ok(rides);
    }

    // 2.2.3 - Driver registration
    @GetMapping(value = "/activate/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetActivationTokenDTO> validateActivationToken(
            @PathVariable String token) {

        GetActivationTokenDTO response = driverService.validateActivationToken(token);
        return ResponseEntity.ok(response);
    }

    // 2.2.3 - Driver registration
    @PostMapping(value = "/activate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPasswordDTO> setDriverPassword(
            @RequestBody UpdateDriverPasswordDTO updateDriverPasswordDTO) {

        UpdatedPasswordDTO response = driverService.setDriverPassword(updateDriverPasswordDTO);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetDriverDTO> getProfile() {

        GetDriverDTO response = driverService.getDriverById(driverId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (Request personal info change)
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping(value = "/profile/change-requests/personal",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> requestPersonalInfoChange(
            @RequestBody UpdateDriverPersonalDTO updateDriverPersonalDTO) {

        CreatedDriverChangeRequestDTO response = driverService.requestPersonalInfoChange(driverId, updateDriverPersonalDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile (Request vehicle info change)
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping(value = "/profile/change-requests/vehicle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> requestVehicleInfoChange(
            @RequestBody UpdateDriverVehicleDTO updateDriverVehicleDTO) {

        CreatedDriverChangeRequestDTO response = driverService.requestVehicleInfoChange(driverId, updateDriverVehicleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile (Request profile picture change)
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping(value = "/profile/change-requests/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> requestProfilePictureChange(
            @RequestParam("file") MultipartFile file) {

        CreatedDriverChangeRequestDTO response = driverService.requestProfilePictureChange(driverId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile (Change driver password)
    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping(value = "/profile/password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPasswordDTO> updatePassword(
            @RequestBody UpdatePasswordDTO updatePasswordDTO) {

        UpdatedPasswordDTO response = driverService.updatePassword(driverId, updatePasswordDTO);
        if (!response.getSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // 2.4.1 - Calling a ride
    @PreAuthorize("hasRole('DRIVER')")
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

    // Update driver's current location
    @PutMapping("/location")
    public ResponseEntity<Void> updateLocation(
            @RequestBody UpdateDriverLocationDTO request
    ) {
        driverService.updateLocation(driverEmail, request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok().build();
    }

    // Optional: Get driver's current location
    @GetMapping("/location")
    public ResponseEntity<UpdateDriverLocationDTO> getLocation() {
        UpdateDriverLocationDTO location = driverService.getLocation(driverEmail);
        return ResponseEntity.ok(location);
    }

    // Optional: Set driver active/inactive
    @PutMapping("/status")
    public ResponseEntity<Void> updateStatus(@RequestParam boolean isActive) {
        driverService.updateActiveStatus(driverEmail, isActive);
        return ResponseEntity.ok().build();
    }

}