package rs.getgo.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.getgo.backend.services.UserProfileService;
import rs.getgo.backend.services.impl.UserProfileServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileServiceImpl userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getProfile(Authentication authentication) {
        Map<String, String> profile = userProfileService.getProfile(authentication);
        return ResponseEntity.ok(profile);
    }
}