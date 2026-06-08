package com.banking.controller;

import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.UserRepository;
import com.banking.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the AuthController (register, login, verify-pin).
 * Uses a real Spring context with an in-memory H2 database to test the full
 * HTTP request/response cycle including validation, security, and error handling.
 */
@SpringBootTest          // Boots the full Spring application context (controllers, services, repos, security)
@AutoConfigureMockMvc    // Creates a MockMvc instance that simulates HTTP requests without starting a real server
@Transactional           // Rolls back each test's database changes so tests don't affect each other
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;            // Simulates HTTP requests to the controller
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    private String customerToken;

    /**
     * Creates an active customer with a known password and PIN before each test.
     * Generates a JWT token for tests that require authentication (e.g. verify-pin).
     */
    @BeforeEach
    void setUp() {
        User customer = new User("Auth", "Tester", "authtest@test.com",
                passwordEncoder.encode("secret"), "333333333", "0600000003", User.Role.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
        // PIN is also BCrypt-encoded, just like the password
        customer.setPin(passwordEncoder.encode("1234"));
        customer = userRepository.save(customer);

        // Pre-generate a valid JWT so verify-pin tests can authenticate
        customerToken = jwtUtil.generateToken(customer.getId(), customer.getEmail());
    }

    // ── Register ────────────────────

    /** A valid registration should succeed and tell the user to wait for employee approval */
    @Test
    void registerReturns201OnSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "New",
                                  "lastName": "User",
                                  "email": "newuser@test.com",
                                  "password": "secret",
                                  "bsn": "444444444",
                                  "phoneNumber": "0600000004"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("Registration successful. Await employee approval."));
    }

    /** Duplicate emails must be rejected to enforce unique user accounts */
    @Test
    void registerReturns409WhenEmailAlreadyInUse() throws Exception {
        // Use the same email as the user created in setUp()
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Duplicate",
                                  "lastName": "User",
                                  "email": "authtest@test.com",
                                  "password": "secret",
                                  "bsn": "555555555",
                                  "phoneNumber": "0600000005"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Login ────────────────────

    /** Valid credentials should return a JWT token and the user's role */
    @Test
    void loginReturns200WithToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "authtest@test.com", "password": "secret" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    /** Wrong password must return 401 to prevent unauthorized access */
    @Test
    void loginReturns401WhenPasswordIsWrong() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "authtest@test.com", "password": "wrong" }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    /** Pending accounts have not been approved yet — they must not be allowed to log in */
    @Test
    void loginReturns401WhenAccountIsPending() throws Exception {
        // Create a separate PENDING user to test this specific scenario
        User pending = new User("Pending", "User", "pending@test.com",
                passwordEncoder.encode("secret"), "666666666", "0600000006", User.Role.CUSTOMER);
        pending.setStatus(UserStatus.PENDING);
        userRepository.save(pending);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "pending@test.com", "password": "secret" }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Verify PIN ────────────────────

    /** Correct PIN should pass — this is the guard before ATM operations */
    @Test
    void verifyPinReturns200WhenCorrect() throws Exception {
        mockMvc.perform(post("/api/auth/verify-pin")
                        // Send the JWT token so the endpoint knows who the caller is
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "pin": "1234" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PIN verified"));
    }

    /** Wrong PIN must be rejected to protect account access */
    @Test
    void verifyPinReturns401WhenIncorrect() throws Exception {
        mockMvc.perform(post("/api/auth/verify-pin")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "pin": "0000" }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Validation rules ────────────────────
    // These tests verify that @Valid / @NotBlank / @Email annotations on the DTO
    // catch bad input BEFORE it reaches the service layer.

    /** Blank email should be caught by @NotBlank before reaching the service */
    @Test
    void loginRejectsBlankEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "", "password": "secret" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("email")));
    }

    /** Malformed email should be caught by @Email before reaching the service */
    @Test
    void loginRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "not-an-email", "password": "secret" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("email")));
    }

    /** Blank password should be caught by @NotBlank before reaching the service */
    @Test
    void loginRejectsBlankPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "john@example.com", "password": "" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("password")));
    }

    /** All register fields are required — blank firstName should be caught by validation */
    @Test
    void registerRejectsBlankFirstName() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "Doe",
                                  "email": "john@example.com",
                                  "password": "secret",
                                  "bsn": "123456789",
                                  "phoneNumber": "0612345678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("firstName")));
    }

    /** Missing password field should be caught by @NotBlank validation */
    @Test
    void registerRejectsMissingPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "email": "john@example.com",
                                  "bsn": "123456789",
                                  "phoneNumber": "0612345678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("password")));
    }
}
