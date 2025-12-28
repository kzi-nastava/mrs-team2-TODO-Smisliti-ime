package rs.getgo.backend.controllers;

import dtos.login.CreateLoginDTO;
import dtos.login.CreatedLoginDTO;
import dtos.user.CreateUserDTO;
import dtos.user.CreatedUserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // 2.2.1 – Login
    @PostMapping("/login")
    public ResponseEntity<CreatedLoginDTO> login(@RequestBody CreateLoginDTO request) {
        CreatedLoginDTO response = new CreatedLoginDTO(1L, "USER", "dummy-token");
        return ResponseEntity.ok(response); // 200 OK
    }

    // 2.2.2 – Register
    @PostMapping("/register")
    public ResponseEntity<CreatedUserDTO> register(@RequestBody CreateUserDTO request) {
        CreatedUserDTO response = new CreatedUserDTO(
                1L, request.getEmail(), request.getName(), request.getSurname()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}