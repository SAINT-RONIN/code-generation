package com.banking.controller;

import com.banking.dto.LoginRequest;
import com.banking.dto.LoginResponse;
import com.banking.dto.RegisterRequest;
import com.banking.dto.VerifyPinRequest;
import com.banking.security.AuthenticatedUser;
import com.banking.service.interfaces.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Handles authentication-related endpoints: registration, login, and PIN verification.
 * All endpoints under /api/auth. Login and register are public; verify-pin requires a valid JWT.
 */
@RestController                         // Combines @Controller + @ResponseBody — returns JSON directly
@RequestMapping("/api/auth")            // Base path for all endpoints in this controller
@Tag(name = "Auth", description = "Authentication and registration endpoints") // Swagger grouping
public class AuthController {

    private final IAuthService authService;

    // Constructor injection — Spring automatically injects the AuthService implementation
    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new customer account with PENDING status.
     * The customer cannot log in until an employee approves them.
     * Returns 201 Created on success.
     */
    @Operation(summary = "Register a new customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        // @Valid triggers Jakarta Bean Validation on the request DTO fields
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful. Await employee approval.");
    }

    /**
     * Authenticates a user by email and password, and returns a JWT token with their role.
     * The frontend stores the token in localStorage and sends it as a Bearer token
     * in the Authorization header on all subsequent requests.
     */
    @Operation(summary = "Log in and get a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or account inactive")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Verifies the customer's ATM PIN. Requires a valid JWT (the user must already be logged in).
     * Used by the ATM interface before allowing deposits or withdrawals.
     */
    @Operation(summary = "Verify the signed-in user's PIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PIN verified"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Incorrect PIN")
    })
    @SecurityRequirement(name = "bearerAuth") // Tells Swagger this endpoint needs a JWT
    @PostMapping("/verify-pin")
    public ResponseEntity<Map<String, String>> verifyPin(
            @Valid @RequestBody VerifyPinRequest request,
            // @AuthenticationPrincipal extracts our custom AuthenticatedUser from the SecurityContext
            @AuthenticationPrincipal AuthenticatedUser caller) {
        authService.verifyPin(caller.getId(), request.pin());
        return ResponseEntity.ok(Map.of("message", "PIN verified"));
    }
}
