
package com.abc.SpringBootSecqurityEx.secqurity;

import com.abc.SpringBootSecqurityEx.entity.User;
import com.abc.SpringBootSecqurityEx.enums.ERole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long expirationTime;

    private SecretKey signingKey;

    @PostConstruct
    void validateConfiguration() {

        if (expirationTime <= 0) {
            throw new IllegalStateException(
                    "JWT expiration must be positive"
            );
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);

            // HS256 requires at least 256 bits (32 bytes).
            if (keyBytes.length < 32) {
                throw new IllegalStateException(
                        "JWT secret must contain at least 32 decoded bytes"
                );
            }

            signingKey = Keys.hmacShaKeyFor(keyBytes);

        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Invalid JWT secret. Provide a valid Base64-encoded secret.",
                    e
            );
        }
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    // Generate Token
    public String generateToken(String username) {
        return buildToken(username, null, null);
    }

    // Generate Token From User
    public String generateTokenFromUsername(User user) {

        List<String> roles = user.getRoles()
                .stream()
                .map(ERole::name)
                .collect(Collectors.toList());

        return buildToken(
                user.getUserName(),
                roles,
                user.getEmail()
        );
    }

    // Common Token Builder
    private String buildToken(
            String username,
            List<String> roles,
            String email
    ) {

        Date now = new Date();
        Date expiration = new Date(
                now.getTime() + expirationTime
        );

        var builder = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiration);

        if (roles != null) {
            builder.claim("roles", roles);
        }

        if (email != null) {
            builder.claim("email", email);
        }

        return builder
                .signWith(getSigningKey())
                .compact();
    }

    // Validate Token
    public boolean validateToken(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            extractAllClaims(token);
            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Extract Username
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extract Claims
    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
