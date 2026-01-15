package rs.getgo.backend.services.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.enums.UserRole;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.dtos.user.CreateUserDTO;
import rs.getgo.backend.dtos.user.CreatedUserDTO;
import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import rs.getgo.backend.services.AuthService;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rs.getgo.backend.utils.TokenUtils;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TokenUtils tokenUtils;

    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    // simple in-memory token store for development (token -> TokenInfo)
    private final Map<String, TokenInfo> resetTokens = new ConcurrentHashMap<>();
    private static final long TOKEN_TTL_SECONDS = 15 * 60; // 15 minutes

    // inject TokenUtils and password encoder (BCrypt strength default 10)
    public AuthServiceImpl(UserRepository userRepository, TokenUtils tokenUtils, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenUtils = tokenUtils;
        this.passwordEncoder = passwordEncoder;
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
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setRole(UserRole.PASSENGER);
        user.setBlocked(false);

        User saved = userRepository.save(user);

        return new CreatedUserDTO(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getSurname(),
                saved.getAddress(),
                saved.getPhoneNumber(),
                saved.isBlocked()
        );
    }

    // LOGIN
    @Override
    public CreatedLoginDTO login(CreateLoginDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials"
                ));

        // verify hashed password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials"
            );
        }

        if (user.isBlocked()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User is blocked"
            );
        }

        // generate JWT token using TokenUtils
        String jwt = tokenUtils.generateToken(user);

        return new CreatedLoginDTO(user.getId(), user.getRole(), jwt);

    }

    // FORGOT PASSWORD
    @Override
    public void forgotPassword(String email) {
        // find user; if not found, return without indicating that to caller (protects against enumeration)
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            long expiry = Instant.now().getEpochSecond() + TOKEN_TTL_SECONDS;
            resetTokens.put(token, new TokenInfo(user.getId(), expiry));

            // build reset link (frontend route)
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            String resetUrl = "http://localhost:4200/user/reset-password?token=" + token + "&email=" + encodedEmail;

            String subject = "GetGo - Password reset";
            String body = "Hello,\n\nTo reset your password click the link below (valid for 15 minutes):\n\n"
                    + resetUrl + "\n\nIf you didn't request this, ignore this email.\n\nRegards,\nGetGo Team";

            // send via Mailpit running on localhost:1025 (simple SMTP)
            try {
                sendViaSmtp("no-reply@getgo.local", email, subject, body, "localhost", 1025);
            } catch (Exception e) {
                // log or ignore in dev; do not leak details to caller
                System.err.println("Failed to send reset email: " + e.getMessage());
            }
        });
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

    // simple SMTP sender (suitable for local Mailpit on 1025)
    private void sendViaSmtp(String from, String to, String subject, String body, String smtpHost, int smtpPort) throws IOException {
        try (Socket socket = new Socket(smtpHost, smtpPort);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII))) {

            String response = reader.readLine(); // server greeting

            sendCmd(writer, reader, "EHLO localhost");
            sendCmd(writer, reader, "MAIL FROM:<" + from + ">");
            sendCmd(writer, reader, "RCPT TO:<" + to + ">");
            sendCmd(writer, reader, "DATA");

            // write headers and body
            writer.write("From: " + from + "\r\n");
            writer.write("To: " + to + "\r\n");
            writer.write("Subject: " + subject + "\r\n");
            writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
            writer.write("\r\n");
            writer.write(body.replace("\n", "\r\n") + "\r\n");
            writer.write(".\r\n");
            writer.flush();
            response = reader.readLine(); // response after data
            sendCmd(writer, reader, "QUIT");
        }
    }

    private void sendCmd(BufferedWriter writer, BufferedReader reader, String cmd) throws IOException {
        writer.write(cmd + "\r\n");
        writer.flush();
        // read response lines until a line that doesn't have '-' after the status code (simple)
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() >= 4 && line.charAt(3) != '-') {
                break;
            }
        }
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
}