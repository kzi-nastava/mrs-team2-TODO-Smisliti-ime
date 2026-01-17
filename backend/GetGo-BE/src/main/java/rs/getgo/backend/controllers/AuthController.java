package rs.getgo.backend.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;
import rs.getgo.backend.dtos.resetPassword.ResetPasswordDTO;
import rs.getgo.backend.dtos.user.CreateUserDTO;
import rs.getgo.backend.dtos.user.CreatedUserDTO;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.services.impl.AuthServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.dtos.user.ForgotPasswordDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Value("${upload.dir}")
    private String uploadDir;

    private final AuthServiceImpl authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthServiceImpl authService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    // 2.2.1 – Login
    @PostMapping("/login")
    public ResponseEntity<CreatedLoginDTO> login(@RequestBody CreateLoginDTO request) {
        CreatedLoginDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // Logout: check whether logout is allowed (frontend sends empty POST)
    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = auth.getName(); // typically email or principal username
        boolean allowed = authService.canLogout(username); // true = allowed to logout; false = blocked (e.g. active driver)
        return ResponseEntity.ok(allowed);
    }

    // 2.2.2 – Register
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<CreatedUserDTO> registerWithFile(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        String profilePictureUrl = null;

        if (file != null && !file.isEmpty()) {
            try {
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir).resolve(filename);
                Files.createDirectories(uploadPath.getParent());
                Files.write(uploadPath, file.getBytes());
                profilePictureUrl = "/uploads/" + filename;
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save profile image");
            }
        }

        CreateUserDTO dto = new CreateUserDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setName(firstName);
        dto.setSurname(lastName);
        dto.setPhone(phone);
        dto.setAddress(address);
        dto.setProfilePictureUrl(profilePictureUrl);

        CreatedUserDTO created = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordDTO request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        Long userId = authService.verifyResetToken(dto.getToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user"));
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @GetMapping("/activate")
    public ResponseEntity<?> activateAccount(@RequestParam("token") String token) {
        boolean activated = authService.activateAccount(token);
        if (activated) {
            return ResponseEntity.ok(Map.of("activated", true, "message", "Account activated"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("activated", false, "message", "Invalid or expired activation token"));
        }
    }
}