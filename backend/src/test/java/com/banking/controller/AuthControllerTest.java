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

    // Creates an active customer with known credentials before each test.
    @BeforeEach
    void setUp() {
        User customer = new User("Auth", "Tester", EMAIL,
                passwordEncoder.encode(PASSWORD), "333333333", "0600000003", User.Role.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
        userRepository.save(customer);
    }

    // ── Register ────────────────────

    // Valid registration returns 201 with a success message.
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

    // Registering with an already-used email returns 409.
    @Test
    void registerReturns409WhenEmailAlreadyInUse() throws Exception {
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

    // Valid credentials return a JWT token and the user's role.
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

    // Wrong password returns 401.
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

    // Pending accounts cannot log in and get 401.
    @Test
    void loginReturns401WhenAccountIsPending() throws Exception {
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

    // ── Validation ────────────────────

    // Blank email is rejected with 400.
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

    // Malformed email is rejected with 400.
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

    // Blank password is rejected with 400.
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

    // Blank first name on register is rejected with 400.
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

    // Missing password on register is rejected with 400.
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
