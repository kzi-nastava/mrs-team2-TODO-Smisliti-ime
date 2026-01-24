package rs.getgo.backend.controllers;

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
import rs.getgo.backend.utils.AuthUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverServiceImpl driverService;

    public DriverController(DriverServiceImpl driverService) {
        this.driverService = driverService;
    }

    // 2.9.2 - Get driver rides
    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping(value = "/rides", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetRideDTO>> getDriverRides(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate) {

        String email = AuthUtils.getCurrentUserEmail();
        List<GetRideDTO> rides = driverService.getDriverRides(email, startDate);
        return ResponseEntity.ok(rides);
    }

    // 2.2.3 - Driver registration (validate activation token)
    @GetMapping(value = "/activate/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetActivationTokenDTO> validateActivationToken(@PathVariable String token) {
        GetActivationTokenDTO response = driverService.validateActivationToken(token);
        return ResponseEntity.ok(response);
    }

    // 2.2.3 - Driver registration (set password)
    @PostMapping(value = "/activate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPasswordDTO> setDriverPassword(
            @RequestBody UpdateDriverPasswordDTO updateDriverPasswordDTO) {

        UpdatedPasswordDTO response = driverService.setDriverPassword(updateDriverPasswordDTO);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (GET)
    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetDriverDTO> getProfile() {
        String email = AuthUtils.getCurrentUserEmail();
        GetDriverDTO response = driverService.getDriver(email);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (Request personal info change)
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping(value = "/profile/change-requests/personal",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> requestPersonalInfoChange(
            @RequestBody UpdateDriverPersonalDTO updateDriverPersonalDTO) {

        String email = AuthUtils.getCurrentUserEmail();
        CreatedDriverChangeRequestDTO response = driverService.requestPersonalInfoChange(email, updateDriverPersonalDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile (Request vehicle info change)
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping(value = "/profile/change-requests/vehicle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> requestVehicleInfoChange(
            @RequestBody UpdateDriverVehicleDTO updateDriverVehicleDTO) {

        String email = AuthUtils.getCurrentUserEmail();
        CreatedDriverChangeRequestDTO response = driverService.requestVehicleInfoChange(email, updateDriverVehicleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile (Request profile picture change)
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping(value = "/profile/change-requests/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverChangeRequestDTO> requestProfilePictureChange(
            @RequestParam("file") MultipartFile file) {

        String email = AuthUtils.getCurrentUserEmail();
        CreatedDriverChangeRequestDTO response = driverService.requestProfilePictureChange(email, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile (Change driver password)
    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping(value = "/profile/password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPasswordDTO> updatePassword(
            @RequestBody UpdatePasswordDTO updatePasswordDTO) {

        String email = AuthUtils.getCurrentUserEmail();
        UpdatedPasswordDTO response = driverService.updatePassword(email, updatePasswordDTO);

        if (!response.getSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // 2.4.1 - Get active driver locations
    @GetMapping(value = "/active-locations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GetActiveDriverLocationDTO>> getActiveDriverLocations() {
        List<GetActiveDriverLocationDTO> response = driverService.getActiveDriverLocations();
        return ResponseEntity.ok(response);
    }

    // Update driver's current location
    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping("/location")
    public ResponseEntity<Void> updateLocation(@RequestBody UpdateDriverLocationDTO request) {
        String email = AuthUtils.getCurrentUserEmail();
        driverService.updateLocation(email, request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok().build();
    }

    // Get driver's current location
    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping("/location")
    public ResponseEntity<UpdateDriverLocationDTO> getLocation() {
        String email = AuthUtils.getCurrentUserEmail();
        UpdateDriverLocationDTO location = driverService.getLocation(email);
        return ResponseEntity.ok(location);
    }

    // Set driver active/inactive
    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping("/status")
    public ResponseEntity<Void> updateStatus(@RequestParam boolean isActive) {
        String email = AuthUtils.getCurrentUserEmail();
        driverService.updateActiveStatus(email, isActive);
        return ResponseEntity.ok().build();
    }
}