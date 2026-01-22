package rs.getgo.backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rs.getgo.backend.model.entities.User;

import java.util.Date;

@Component
public class TokenUtils {

    @Value("${jwt.appName}")
    private String APP_NAME;

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.expiresIn}")
    private int EXPIRES_IN;

    @Value("${jwt.authHeader}")
    private String AUTH_HEADER;

    private final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public String generateToken(User user) {
        return Jwts.builder()
                .setIssuer(APP_NAME)
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRES_IN))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)), SIGNATURE_ALGORITHM)
                .compact();
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public String getUsernameFromToken(String token) {
        try {
            return getAllClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        String email = getUsernameFromToken(token);
        return email != null && email.equals(userDetails.getUsername());
    }

    public int getExpiredIn() {
        return EXPIRES_IN;
    }

    public String getRoleFromToken(String token) {
        try {
            return getAllClaims(token).get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String generateRatingToken(Long rideId, Long passengerId) {
        return Jwts.builder()
                .claim("rideId", rideId)
                .claim("passengerId", passengerId)
                .claim("type", "RATING")
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000 * 3)) // 72h
                .signWith(SignatureAlgorithm.HS256, Decoders.BASE64.decode(SECRET))
                .compact();
    }


    public RatingTokenData parseRatingToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(Decoders.BASE64.decode(SECRET))
                .parseClaimsJws(token)
                .getBody();

        return new RatingTokenData(
                claims.get("rideId", Long.class),
                claims.get("passengerId", Long.class)
        );
    }




}