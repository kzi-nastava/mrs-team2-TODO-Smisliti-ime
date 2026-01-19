package rs.getgo.backend.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.services.FileStorageService;
import rs.getgo.backend.services.UserProfileService;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.upload.base-url}")
    private String uploadBaseUrl;

    public UserProfileServiceImpl(UserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
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

        String fullName = user.getName() + " " + user.getSurname();
        response.put("fullName", fullName);

        String imagePath;

        if (user instanceof Passenger p && p.getProfilePictureUrl() != null) {
            imagePath = p.getProfilePictureUrl();
        } else if (user instanceof Driver d && d.getProfilePictureUrl() != null) {
            imagePath = d.getProfilePictureUrl();
        } else {
            imagePath = "/uploads/" + fileStorageService.getDefaultProfilePicture();
        }

        response.put("profilePictureUrl", uploadBaseUrl + imagePath);

        return response;
    }
}