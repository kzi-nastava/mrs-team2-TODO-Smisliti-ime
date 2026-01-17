package rs.getgo.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.passenger.GetPassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatePassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatedPassengerDTO;
import rs.getgo.backend.dtos.user.UpdatedProfilePictureDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.getgo.backend.services.PassengerServiceImpl;

@RestController
@RequestMapping("/api/passenger")
public class PassengerController {

    @Autowired
    private PassengerServiceImpl passengerService;

    Long passengerId = 5L; // TODO: get from cookie/whatever we decide to use

    // 2.3 - User profile
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetPassengerDTO> getProfile() {

        GetPassengerDTO response = passengerService.getPassengerById(passengerId);

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PutMapping(value = "/profile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPassengerDTO> updateProfile(
            @RequestBody UpdatePassengerDTO updatePassengerDTO) {

        UpdatedPassengerDTO response = passengerService.updateProfile(passengerId, updatePassengerDTO);

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (Change passenger password)
    @PutMapping(value = "/profile/password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPasswordDTO> updatePassword(
            @RequestBody UpdatePasswordDTO updatePasswordDTO) {

        UpdatedPasswordDTO response = passengerService.updatePassword(passengerId, updatePasswordDTO);
        if (!response.getSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PostMapping(value = "/profile/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedProfilePictureDTO> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {

        UpdatedProfilePictureDTO response = passengerService.uploadProfilePicture(passengerId, file);

        return ResponseEntity.ok(response);
    }
}
