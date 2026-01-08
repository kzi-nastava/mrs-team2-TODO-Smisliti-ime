package rs.getgo.backend.controllers;

import rs.getgo.backend.dtos.passenger.GetPassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatePassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatedPassengerDTO;
import rs.getgo.backend.dtos.user.UpdatedProfilePictureDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/passenger")
public class PassengerController {

    // 2.3 - User profile
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetPassengerDTO> getProfile() {

        GetPassengerDTO response = new GetPassengerDTO();
        response.setId(1L);
        response.setEmail("passenger@example.com");
        response.setName("Passenger");

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PutMapping(value = "/profile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPassengerDTO> updateProfile(
            @RequestBody UpdatePassengerDTO request) {

        UpdatedPassengerDTO response = new UpdatedPassengerDTO();
        response.setId(1L);
        response.setName(request.getName());

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PostMapping(value = "/profile/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedProfilePictureDTO> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {

        UpdatedProfilePictureDTO response = new UpdatedProfilePictureDTO();
        response.setPictureUrl("/uploads/passenger_123.jpg");

        return ResponseEntity.ok(response);
    }
}
