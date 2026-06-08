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

/**
 * Utility class for creating and parsing HMAC-signed JSON Web Tokens (JWTs).
 * Used for stateless authentication — no server-side session is stored.
 * The JWT secret and expiration time are configured in application.properties.
 */
@Component // Registers this class as a Spring-managed bean so it can be injected elsewhere
public class JwtUtil {

    // The HMAC key used to sign and verify tokens
    private final SecretKey signingKey;

    // How long (in milliseconds) a token stays valid after creation
    private final long expirationMs;

    /**
     * Constructor — Spring injects the values from application.properties.
     *
     * @param secret       Base64-encoded HMAC secret (jwt.secret property)
     * @param expirationMs token lifetime in milliseconds (jwt.expiration property)
     */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationMs) {
        // Decode the Base64 string into raw bytes, then create an HMAC-SHA signing key
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a signed JWT containing the user's email (as the "subject") and their database ID.
     * The token is used by the frontend as a Bearer token in the Authorization header.
     *
     * @param userId the database primary key of the user
     * @param email  the user's email, stored as the JWT subject
     * @return a compact signed JWT string
     */
    public String generateToken(Long userId, String email) {
        return Jwts.builder()
                .subject(email)                                            // Standard JWT "sub" claim
                .claim("userId", userId)                                   // Custom claim for fast ID lookup
                .issuedAt(new Date())                                      // Token creation timestamp
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // Expiry timestamp
                .signWith(signingKey)                                      // Sign with our HMAC secret
                .compact();                                                // Serialize to a Base64 string
    }

    /**
     * Parses and verifies a JWT token. If the signature is invalid or the token is expired,
     * the jjwt library throws a JwtException which the caller must handle.
     *
     * @param token the raw JWT string from the Authorization header
     * @return the decoded claims (subject, userId, expiration, etc.)
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)     // Verify the HMAC signature matches
                .build()
                .parseSignedClaims(token)   // Parse and validate expiration
                .getPayload();              // Return the decoded claims body
    }
}
