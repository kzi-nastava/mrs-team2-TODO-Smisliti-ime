package controllers;

import dtos.requests.LoginRequestDTO;
import dtos.requests.RegisterRequestDTO;
import dtos.responses.LoginResponseDTO;
import dtos.responses.UserResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO request) {

        LoginResponseDTO response =
                new LoginResponseDTO(1L, "USER", "dummy-token");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @RequestBody RegisterRequestDTO request) {

        UserResponseDTO response =
                new UserResponseDTO(1L, request.getEmail(),
                        request.getName(), request.getSurname());

        return ResponseEntity.status(201).body(response);
    }
}