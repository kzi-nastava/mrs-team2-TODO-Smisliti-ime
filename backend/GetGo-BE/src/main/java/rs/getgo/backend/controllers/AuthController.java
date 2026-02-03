package rs.getgo.backend.controllers;

import jakarta.validation.Valid;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.dtos.user.ForgotPasswordDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;
import rs.getgo.backend.services.FileStorageService;
import rs.getgo.backend.utils.AuthUtils;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthServiceImpl authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public AuthController(AuthServiceImpl authService,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         FileStorageService fileStorageService) {
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    // 2.2.1 – Login
    @PostMapping("/login")
    public ResponseEntity<CreatedLoginDTO> login(@Valid @RequestBody CreateLoginDTO request) {
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
        String email = AuthUtils.getCurrentUserEmail();
        String role = AuthUtils.getCurrentUserRole();
        boolean allowed = authService.canLogout(email, role); // true = allowed to logout; false = blocked (e.g. active driver)
        return ResponseEntity.ok(allowed);
    }

    // 2.2.2 – Register
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<CreatedUserDTO> registerWithFile(
            @Valid @ModelAttribute CreateUserDTO dto,
            @RequestParam(value = "file", required = false) MultipartFile file
    )
    {
        String profilePictureUrl = null;

        if (file != null && !file.isEmpty()) {
            profilePictureUrl = fileStorageService.storeFile(file, "temp");
        }

        dto.setProfilePictureUrl(profilePictureUrl);

        CreatedUserDTO created = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
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

    // Keep JSON for mobile app / API clients
    @GetMapping(value = "/activate-mobile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> activateAccountMobileJson(@RequestParam("token") String token) {
        boolean activated = authService.activateAccount(token);
        if (activated) {
            return ResponseEntity.ok(
                    Map.of(
                            "activated", true,
                            "message", "Account activated successfully"
                    )
            );
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        Map.of(
                                "activated", false,
                                "message", "Invalid or expired activation token"
                        )
                );
    }
/*
    // Add a browser-friendly HTML response (so the link “leads somewhere”)
    @GetMapping(value = "/activate-mobile", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> activateAccountMobileHtml(@RequestParam("token") String token) {
        boolean activated = authService.activateAccount(token);

        String title = activated ? "GetGo - Account activated" : "GetGo - Activation failed";
        String body = activated
                ? "<h2>Your account has been activated.</h2><p>You can return to the app and log in.</p>"
                : "<h2>Activation link is invalid or expired.</h2><p>Please register again.</p>";

        String html =
                "<!doctype html><html><head><meta charset='utf-8'/>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1'/>" +
                "<title>" + title + "</title></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 24px;'>" +
                "<h1>" + title + "</h1>" +
                body +
                "</body></html>";

        return ResponseEntity
                .status(activated ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }*/

    // Mobile/browser entry point for password reset (email link)
    // - Browser: shows simple HTML + deep link into app
    // - App/API: returns JSON about token validity
    @GetMapping(value = "/reset-password-mobile", produces = {MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> resetPasswordMobileEntry(
            @RequestParam("token") String token,
            @RequestHeader(value = "Accept", required = false) String accept
    ) {
        boolean valid;
        try {
            authService.verifyResetToken(token); // throws / returns userId; we only care it’s valid
            valid = true;
        } catch (Exception ex) {
            valid = false;
        }

        // If client explicitly wants JSON (mobile app calling), return JSON.
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            if (valid) {
                return ResponseEntity.ok(Map.of("valid", true, "token", token));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("valid", false, "message", "Invalid or expired reset token"));
        }

        // Default: browser-friendly HTML
        String title = valid ? "GetGo - Reset password" : "GetGo - Reset link invalid";
        String deepLink = "getgo://reset-password/?token=" + token;

        String body = valid
                ? ("<p>Open the GetGo app to set a new password.</p>"
                   + "<p><a href='" + deepLink + "'>Open in GetGo app</a></p>"
                   + "<p>If the app doesn’t open, copy this token into the app reset screen:</p>"
                   + "<pre style='padding:12px;background:#f5f5f5;border-radius:6px;\">" + token + "</pre>")
                : "<p>This reset link is invalid or expired. Please request a new password reset.</p>";

        String html =
                "<!doctype html><html><head><meta charset='utf-8'/>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1'/>" +
                        "<title>" + title + "</title></head>" +
                        "<body style='font-family: Arial, sans-serif; padding: 24px;'>" +
                        "<h1>" + title + "</h1>" +
                        body +
                        "</body></html>";

        return ResponseEntity
                .status(valid ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}