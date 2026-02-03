package rs.getgo.backend.controllers;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.passenger.GetPassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatePassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatedPassengerDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import rs.getgo.backend.dtos.user.UpdatedProfilePictureDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.getgo.backend.services.PassengerService;
import rs.getgo.backend.utils.AuthUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/passenger")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) { this.passengerService = passengerService; }

    // 2.3 - User profile
    @PreAuthorize("hasRole('PASSENGER')")
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetPassengerDTO> getProfile() {
        String email = AuthUtils.getCurrentUserEmail();
        GetPassengerDTO response = passengerService.getPassenger(email);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PreAuthorize("hasRole('PASSENGER')")
    @PutMapping(value = "/profile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPassengerDTO> updateProfile(
            @Valid @RequestBody UpdatePassengerDTO updatePassengerDTO) {
        String email = AuthUtils.getCurrentUserEmail();
        UpdatedPassengerDTO response = passengerService.updateProfile(email, updatePassengerDTO);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (Change passenger password)
    @PreAuthorize("hasRole('PASSENGER')")
    @PutMapping(value = "/profile/password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPasswordDTO> updatePassword(
            @Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        String email = AuthUtils.getCurrentUserEmail();
        UpdatedPasswordDTO response = passengerService.updatePassword(email, updatePasswordDTO);
        if (!response.getSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PreAuthorize("hasRole('PASSENGER')")
    @PostMapping(value = "/profile/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedProfilePictureDTO> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {
        String email = AuthUtils.getCurrentUserEmail();
        UpdatedProfilePictureDTO response = passengerService.uploadProfilePicture(email, file);
        return ResponseEntity.ok(response);
    }

    // 2.9.1 - Get passenger rides
    @PreAuthorize("hasRole('PASSENGER')")
    @GetMapping(value = "/rides", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetRideDTO>> getPassengerRides(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate) {

        String email = AuthUtils.getCurrentUserEmail();
        List<GetRideDTO> rides = passengerService.getPassengerRides(email, startDate);
        return ResponseEntity.ok(rides);
    }

    // 2.9.2 - Get single passenger ride by id
    @PreAuthorize("hasRole('PASSENGER')")
    @GetMapping(value = "/rides/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRideDTO> getPassengerRideById(@PathVariable Long id) {
        String email = AuthUtils.getCurrentUserEmail();
        GetRideDTO ride = passengerService.getPassengerRideById(email, id);
        return ResponseEntity.ok(ride);
    }
}
