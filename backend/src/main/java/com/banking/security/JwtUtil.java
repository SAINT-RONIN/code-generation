package com.banking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/** Creates, validates, and parses HMAC-signed JWTs for stateless authentication. */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationMs) {
        // Secret is stored Base64-encoded in application.properties
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationMs = expirationMs;
    }

    /** @return a signed JWT containing the caller's email as subject plus stable identity claims */
    public String generateToken(Long userId, String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    /** @return the email (subject) embedded in the token */
    public String extractEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /** @return the internal user id embedded in the token */
    public Long extractUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /** @return true if the token has a valid signature and has not expired */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            // Covers expired, malformed, and tampered tokens
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
