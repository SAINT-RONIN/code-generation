package com.banking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security filter that intercepts every HTTP request and checks for a valid JWT
 * in the Authorization header. When the token is valid, the filter populates the
 * SecurityContext so that downstream controllers and @PreAuthorize checks can identify
 * the caller.
 *
 * Extends OncePerRequestFilter to guarantee it runs exactly once per request, even
 * if the request is forwarded internally (e.g. error dispatch).
 */
@Component // Registers this filter as a Spring bean so SecurityConfig can wire it in
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Constructor injection — Spring auto-wires JwtUtil and our AppUserDetailsService
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Core filter logic — runs on every request.
     * Extracts the Bearer token, validates it, and sets up the SecurityContext.
     * If no token is present or it is invalid, the request continues as unauthenticated
     * (Spring Security will return 401/403 if the endpoint requires authentication).
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractBearerToken(request);
        if (token != null) {
            try {
                // Parse and verify the JWT, then set up the authenticated user
                setAuthenticationFromToken(jwtUtil.parseClaims(token), request);
            } catch (JwtException ignored) {
                // Invalid or expired tokens are silently ignored — the request
                // continues as unauthenticated so Spring Security handles the 401
            }
        }
        // Always continue the filter chain, whether authenticated or not
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT string from the "Authorization: Bearer <token>" header.
     *
     * @return the token string, or null if the header is missing or not a Bearer token
     */
    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // Strip the "Bearer " prefix (7 characters)
        }
        return null;
    }

    /**
     * Loads the user from the database, verifies the token's userId claim matches,
     * and stores the authenticated principal in the SecurityContext.
     *
     * We verify the userId from the token against the database to prevent using a token
     * from a deleted or re-created account with the same email.
     */
    private void setAuthenticationFromToken(Claims claims, HttpServletRequest request) {
        String email = claims.getSubject();
        Long userId = claims.get("userId", Long.class);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Ensure the loaded user is our custom AuthenticatedUser and the IDs match
        if (!(userDetails instanceof AuthenticatedUser authenticatedUser) || !authenticatedUser.getId().equals(userId)) {
            return; // Token is stale or doesn't match — skip authentication
        }

        // Build the authentication token with the user's granted authorities (roles)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());

        // Attach HTTP request details (remote address, session ID) for audit/logging
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Store in the SecurityContext so @PreAuthorize and @AuthenticationPrincipal can access it
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
