package rs.getgo.backend.services;

import org.springframework.security.core.Authentication;

import java.util.Map;

public interface UserProfileService {

    Map<String, String> getProfile(Authentication authentication);
}
