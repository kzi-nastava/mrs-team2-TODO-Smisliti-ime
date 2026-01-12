package rs.getgo.backend.services;

import org.springframework.stereotype.Service;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.dtos.user.CreateUserDTO;
import rs.getgo.backend.dtos.user.CreatedUserDTO;
import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AuthService {

    private final UserRepository userRepository;

    // simple in-memory token store for development (token -> TokenInfo)
    private final Map<String, TokenInfo> resetTokens = new ConcurrentHashMap<>();
    private static final long TOKEN_TTL_SECONDS = 15 * 60; // 15 minutes

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // REGISTER
    public CreatedUserDTO register(CreateUserDTO request) {

        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email already in use"
                    );
                });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getName());
        user.setLastName(request.getSurname());
        user.setPhoneNumber(request.getPhone());
        user.setBlocked(false);

        User saved = userRepository.save(user);

        return new CreatedUserDTO(
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName()
        );
    }

    // LOGIN
    public CreatedLoginDTO login(CreateLoginDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials"
                ));

        if (!request.getPassword().equals(user.getPassword())) {
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

        String token = UUID.randomUUID().toString();

        return new CreatedLoginDTO(
                user.getId(),
                token
        );
    }

    // FORGOT PASSWORD
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