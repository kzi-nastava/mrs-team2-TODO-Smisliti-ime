package rs.getgo.backend.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

/**
 * Utility class for extracting authenticated user information from Spring Security context.
 * The JWT token is already parsed by TokenAuthenticationFilter and stored in SecurityContextHolder.
 */
public class AuthUtils {

    /**
     * Get the email of the currently authenticated user.
     * This reads from the SecurityContextHolder which is populated by TokenAuthenticationFilter.
     *
     * @return email from JWT subject claim
     * @throws IllegalStateException if no authenticated user found
     */
    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("No authenticated user found");
        }

        return auth.getName(); // This is the email from JWT "sub" claim
    }

    /**
     * Get the role of the currently authenticated user.
     *
     * @return role without "ROLE_" prefix (e.g., "DRIVER", "PASSENGER", "ADMIN")
     * @throws IllegalStateException if no authenticated user or role found
     */
    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No role found for user"));
    }

    /**
     * Check if the current user has a specific role.
     *
     * @param role the role to check (without "ROLE_" prefix)
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        try {
            String userRole = getCurrentUserRole();
            return userRole.equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the current Authentication object.
     * Useful for advanced use cases.
     *
     * @return Authentication object or null if not authenticated
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}