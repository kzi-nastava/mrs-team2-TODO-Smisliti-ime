package rs.getgo.backend.controllers;

import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;
import rs.getgo.backend.dtos.user.CreateUserDTO;
import rs.getgo.backend.dtos.user.CreatedUserDTO;
import rs.getgo.backend.services.Impl.AuthServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.dtos.user.ForgotPasswordDTO;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200") // allow Angular dev server
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    // 2.2.1 – Login
    @PostMapping("/login")
    public ResponseEntity<CreatedLoginDTO> login(@RequestBody CreateLoginDTO request) {
        CreatedLoginDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // 2.2.2 – Register
    @PostMapping("/register")
    public ResponseEntity<CreatedUserDTO> register(@RequestBody CreateUserDTO request) {
        CreatedUserDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Forgot password - sends reset link via Mailpit (development SMTP)
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordDTO request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }
}