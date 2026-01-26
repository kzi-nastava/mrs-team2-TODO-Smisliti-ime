package rs.getgo.backend.services.impl;

import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.enums.UserRole;
import rs.getgo.backend.repositories.PassengerRepository;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.dtos.user.CreateUserDTO;
import rs.getgo.backend.dtos.user.CreatedUserDTO;
import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import rs.getgo.backend.services.AuthService;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.services.EmailService;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rs.getgo.backend.utils.TokenUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenUtils tokenUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final DriverRepository driverRepo;
    private final EmailService emailService;
    private final PassengerRepository passengerRepository;

    // simple in-memory token store for development (token -> TokenInfo)
    private final Map<String, TokenInfo> resetTokens = new ConcurrentHashMap<>();
    private static final long RESET_TOKEN_TTL_SECONDS = 15 * 60; // 15 minutes

    // activation tokens (token -> TokenInfo) for email activation (24h)
    private final Map<String, TokenInfo> activationTokens = new ConcurrentHashMap<>();
    private static final long ACTIVATION_TTL_SECONDS = 24 * 60 * 60; // 24 hours

    // inject TokenUtils and password encoder (BCrypt strength default 10)
    public AuthServiceImpl(
            UserRepository userRepository,
            TokenUtils tokenUtils,
            BCryptPasswordEncoder passwordEncoder,
            DriverRepository driverRepo,
            EmailService emailService,
            PassengerRepository passengerRepository
    ) {
        this.userRepository = userRepository;
        this.tokenUtils = tokenUtils;
        this.passwordEncoder = passwordEncoder;
        this.driverRepo = driverRepo;
        this.emailService = emailService;
        this.passengerRepository = passengerRepository;
    }

    // REGISTER
    @Override
    public CreatedUserDTO register(CreateUserDTO request) {

        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email already in use"
                    );
                });

        Passenger user = new Passenger();
        user.setEmail(request.getEmail());
        // hash password before saving
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(UserRole.PASSENGER);
        user.setProfilePictureUrl(request.getProfilePictureUrl()); // preserve profile picture if sent

        // newly registered users must activate via email before they can login
        user.setBlocked(false);            // not admin-blocked by default
        user.setCanAccessSystem(false);    // CANNOT access until activation (this is correct)

        Passenger saved = passengerRepository.save(user);  // Fixed: save as Passenger via passengerRepository

        // generate activation token (24h) and send activation email via EmailService
        String activationToken = UUID.randomUUID().toString();
        long expiry = Instant.now().getEpochSecond() + ACTIVATION_TTL_SECONDS;
        activationTokens.put(activationToken, new TokenInfo(saved.getId(), expiry));

        try {
            emailService.sendActivationEmail(saved.getEmail(), activationToken);
            System.out.println("Activation email sent to: " + saved.getEmail() + " with token: " + activationToken);
        } catch (Exception e) {
            System.err.println("Failed to send activation email: " + e.getMessage());
            e.printStackTrace();
        }

        return new CreatedUserDTO(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getSurname(),
                saved.getAddress(),
                saved.getPhone(),
                saved.isBlocked(),
                saved.getProfilePictureUrl()
        );
    }

    // LOGIN
    @Override
    public CreatedLoginDTO login(CreateLoginDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Email not found"
                ));

        // verify hashed password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid password"
            );
        }

        if (user.isBlocked()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User is blocked"
            );
        }

        if (user.getRole() == UserRole.PASSENGER) {
            Passenger passenger = (Passenger) user;
            if (!passenger.isCanAccessSystem()) {
                // check if there exists a valid activation token for this user
                boolean hasValidActivation = activationTokens.values().stream()
                        .anyMatch(ti -> ti.userId.equals(user.getId()) && Instant.now().getEpochSecond() <= ti.expirySeconds);

                if (!hasValidActivation) {
                    // activation window expired -> remove the unactivated user and deny login
                    try {
                        userRepository.deleteById(user.getId());
                        System.out.println("Deleted expired unactivated user: " + user.getEmail());
                    } catch (Exception ignored) {
                    }
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Activation expired. Please register again."
                    );
                } else {
                    // user still within activation window but not activated
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Account not activated. Check your email for activation link."
                    );
                }
            }
        }

        if (user.getRole() == UserRole.DRIVER) {
            Driver driver = (Driver) user;
            driver.setActive(true);
            driverRepo.save(driver);
        }

        // generate JWT token using TokenUtils
        String jwt = tokenUtils.generateToken(user);

        return new CreatedLoginDTO(user.getId(), user.getRole(), jwt);

    }

    // FORGOT PASSWORD
    @Override
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Email not found"
                ));

        String token = UUID.randomUUID().toString();
        long expiry = Instant.now().getEpochSecond() + RESET_TOKEN_TTL_SECONDS;
        resetTokens.put(token, new TokenInfo(user.getId(), expiry));

        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String resetUrl = "http://localhost:4200/user/reset-password?token=" + token + "&email=" + encodedEmail;

        try {
            emailService.sendResetEmail(email, resetUrl);
        } catch (Exception e) {
            System.err.println("Failed to send reset email: " + e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send reset email"
            );
        }
    }

    // verify token (optional helper for reset endpoint you may implement later)
    @Override
    public Long verifyResetToken(String token) {
        TokenInfo info = resetTokens.get(token);
        if (info == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }
        if (Instant.now().getEpochSecond() > info.expirySeconds) {
            resetTokens.remove(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }
        return info.userId;
    }

    // Activation endpoint used by frontend -> controller should call this service method
    public boolean activateAccount(String token) {
        System.out.println("Attempting activation with token: " + token);
        TokenInfo info = activationTokens.get(token);
        if (info == null) {
            System.err.println("Activation token not found: " + token);
            return false;
        }
        if (Instant.now().getEpochSecond() > info.expirySeconds) {
            activationTokens.remove(token);
            System.err.println("Activation token expired: " + token);
            // expired -> delete unactivated user
            try {
                userRepository.deleteById(info.userId);
            } catch (Exception ignored) {}
            return false;
        }
        // activate user
        User user = userRepository.findById(info.userId).orElse(null);
        if (user == null) {
            activationTokens.remove(token);
            System.err.println("User not found for activation token: " + token);
            return false;
        }
        // allow access after activation
        if (user instanceof Passenger) {
            Passenger passenger = (Passenger) user;
            passenger.setCanAccessSystem(true);  // GRANT access
            passengerRepository.save(passenger);
            System.out.println("User activated successfully: " + user.getEmail());
        }
        activationTokens.remove(token);
        return true;
    }

    // internal token info
    private static class TokenInfo {
        final Long userId;
        final long expirySeconds;
        TokenInfo(Long userId, long expirySeconds) {
            this.userId = userId;
            this.expirySeconds = expirySeconds;
        }
    }

    public boolean canLogout(String email, String role) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return true;

        if (!role.contains("DRIVER")) return true;

        Optional<Driver> driver = driverRepo.findById(user.getId());
        return driver.map(value -> Boolean.FALSE.equals(value.getActive())).orElse(true);
    }
}
