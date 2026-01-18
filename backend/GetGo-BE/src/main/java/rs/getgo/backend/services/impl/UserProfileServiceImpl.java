package rs.getgo.backend.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.services.UserProfileService;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;

    @Value("${app.upload.base-url}")
    private String uploadBaseUrl;

    public UserProfileServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, String> getProfile(Authentication authentication) {
        Map<String, String> response = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Full name
        String fullName = user.getName() + " " + user.getSurname();
        response.put("fullName", fullName);

        // Profile picture - default to frontend asset if no custom picture
        String profilePictureUrl = "http://localhost:4200/assets/images/sussy_cat.jpg"; // default
        if (user instanceof Passenger p && p.getProfilePictureUrl() != null) {
            profilePictureUrl = uploadBaseUrl + p.getProfilePictureUrl();
        } else if (user instanceof Driver d && d.getProfilePictureUrl() != null) {
            profilePictureUrl = uploadBaseUrl + d.getProfilePictureUrl();
        }

        response.put("profilePictureUrl", profilePictureUrl);

        return response;
    }
}