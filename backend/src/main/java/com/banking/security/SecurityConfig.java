package com.banking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Central Spring Security configuration for the banking API.
 * Sets up stateless JWT authentication, CORS rules, and public/protected endpoint mappings.
 */
@Configuration       // Marks this class as a source of @Bean definitions
@EnableMethodSecurity // Enables @PreAuthorize annotations on controller methods (e.g. hasRole('EMPLOYEE'))
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Comma-separated list of allowed frontend origins (e.g. "http://localhost:5173,https://myapp.vercel.app")
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    // Endpoints that do not require a JWT token (login, register, docs, H2 console)
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/h2-console/**",      // H2 database console (development only)
            "/swagger-ui/**",      // Swagger UI static resources
            "/swagger-ui.html",    // Swagger UI entry point
            "/v3/api-docs/**"      // OpenAPI JSON specification
    };

    // Constructor injection of the JWT filter
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Provides the BCrypt password encoder used to hash passwords before storing them
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Exposes Spring's AuthenticationManager so the auth service can use it for login validation
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) to allow the frontend application
     * to call this API from the browser. Without this, the browser blocks requests from
     * a different origin (e.g. localhost:5173 calling localhost:8080).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Split the comma-separated origins string into a list
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));     // Allow any header (including Authorization)
        config.setAllowCredentials(true);            // Allow cookies/auth headers
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Apply to all endpoints
        return source;
    }

    /**
     * Builds the main security filter chain that processes every HTTP request.
     * Key decisions:
     * - CSRF is disabled because we use stateless JWT tokens (no cookies to protect)
     * - Sessions are STATELESS so Spring never creates an HttpSession
     * - The JWT filter runs before Spring's default UsernamePasswordAuthenticationFilter
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Disable CSRF — not needed for stateless JWT auth (tokens are sent in headers, not cookies)
                .csrf(AbstractHttpConfigurer::disable)
                // Disable X-Frame-Options so the H2 console can render in an iframe (dev only)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // Never create HTTP sessions — each request is authenticated independently via JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints can be accessed without a token
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // All other endpoints require a valid JWT
                        .anyRequest().authenticated()
                )
                // Insert our JWT filter before Spring's default username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
