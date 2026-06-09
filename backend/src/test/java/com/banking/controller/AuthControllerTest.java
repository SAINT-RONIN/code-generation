package com.banking.controller;

import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.UserRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    private static final String EMAIL = "authtest@test.com";
    private static final String PASSWORD = "secret";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /** Creates an active customer with a known password before each test. */
    @BeforeEach
    void setUp() {
        User customer = new User("Auth", "Tester", EMAIL,
                passwordEncoder.encode(PASSWORD), "333333333", "0600000003", User.Role.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
        userRepository.save(customer);
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
                                  "email": "%s",
                                  "password": "%s",
                                  "bsn": "555555555",
                                  "phoneNumber": "0600000005"
                                }
                                """.formatted(EMAIL, PASSWORD)))
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
                                { "email": "%s", "password": "%s" }
                                """.formatted(EMAIL, PASSWORD)))
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
                                { "email": "%s", "password": "wrong" }
                                """.formatted(EMAIL)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    /** Pending accounts have not been approved yet — they must not be allowed to log in */
    @Test
    void loginReturns401WhenAccountIsPending() throws Exception {
        // Create a separate PENDING user to test this specific scenario
        User pending = new User("Pending", "User", "pending@test.com",
                passwordEncoder.encode(PASSWORD), "666666666", "0600000006", User.Role.CUSTOMER);
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
