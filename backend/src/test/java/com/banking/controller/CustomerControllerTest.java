package com.banking.controller;

import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.AccountRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the CustomerController (employee-only customer management).
 * Tests the full HTTP cycle for listing, approving, closing, and updating customers.
 * Verifies role-based access control (@PreAuthorize) and input validation.
 */
@SpringBootTest          // Boots full Spring context with real services and H2 database
@AutoConfigureMockMvc    // Provides MockMvc to simulate HTTP requests
@Transactional           // Rolls back database changes after each test
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    private String employeeToken;    // JWT for the employee — has access to customer management
    private String customerToken;    // JWT for a customer — should be denied access
    private Long pendingCustomerId;  // ID of a PENDING customer used in approval/close tests

    /**
     * Creates test data before each test:
     * - A PENDING customer (for approval and close tests)
     * - An ACTIVE customer with a JWT (to verify customers are blocked from employee endpoints)
     * - An employee JWT (from the seeded employee user)
     */
    @BeforeEach
    void setUp() {
        // Create a pending customer who can be approved or closed during tests
        User pending = new User("Pending", "Customer", "pendingcust@test.com",
                passwordEncoder.encode("pass"), "888888888", "0600000008", User.Role.CUSTOMER);
        pending.setStatus(UserStatus.PENDING);
        pending = userRepository.save(pending);
        pendingCustomerId = pending.getId();

        // Load the seeded employee (created by DataInitializer) and generate their JWT
        User employee = userRepository.findByEmail("employee@bank.com").orElseThrow();
        employeeToken = jwtUtil.generateToken(employee.getId(), employee.getEmail());

        // Create an active customer to test that customer role is denied access
        User customer = new User("Active", "Customer", "activecust@test.com",
                passwordEncoder.encode("pass"), "999999999", "0600000009", User.Role.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
        customer = userRepository.save(customer);
        customerToken = jwtUtil.generateToken(customer.getId(), customer.getEmail());
    }

    // ── GET /api/customers (employee only) ────────────────────

    /** Employees need to browse customers for approval and management */
    @Test
    void getCustomersReturnsPageForEmployee() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /** Customer data is sensitive — only employees should access the list */
    @Test
    void getCustomersReturns403ForCustomer() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/customers/{id} — approve pending customer ────────────────────

    /** Approving a pending customer should set them to ACTIVE and create their bank accounts */
    @Test
    void approvePendingCustomerActivatesAndCreatesAccounts() throws Exception {
        mockMvc.perform(put("/api/customers/" + pendingCustomerId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "ACTIVE", "dailyLimit": 3000.00, "absoluteLimit": -100.00 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify the status change actually persisted to the database
        assertEquals(UserStatus.ACTIVE, userRepository.findById(pendingCustomerId).orElseThrow().getStatus());
        // Verify that checking + savings accounts were created for this customer
        assertFalse(accountRepository.findByUserIdAndActiveTrue(pendingCustomerId).isEmpty());
    }

    // ── PUT /api/customers/{id} — close customer ────────────────────

    /** Closing a customer should set their status to CLOSED (first approve, then close) */
    @Test
    void closeCustomerDeactivatesAccounts() throws Exception {
        // Step 1: First approve the pending customer so they have accounts
        mockMvc.perform(put("/api/customers/" + pendingCustomerId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "ACTIVE", "dailyLimit": 2000.00, "absoluteLimit": 0.00 }
                                """))
                .andExpect(status().isOk());

        // Step 2: Now close them — this should deactivate all their accounts
        mockMvc.perform(put("/api/customers/" + pendingCustomerId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CLOSED" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        assertEquals(UserStatus.CLOSED, userRepository.findById(pendingCustomerId).orElseThrow().getStatus());
    }

    // ── PUT /api/customers/{id} — not found ────────────────────

    /** Updating a non-existent customer should return 404 */
    @Test
    void updateCustomerReturns404WhenNotFound() throws Exception {
        mockMvc.perform(put("/api/customers/99999")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "ACTIVE" }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Validation rules ────────────────────

    /** Daily limit must be zero or positive — negative values should be rejected by @Min(0) */
    @Test
    void updateCustomerRejectsNegativeDailyLimit() throws Exception {
        mockMvc.perform(put("/api/customers/" + pendingCustomerId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "dailyLimit": -100.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("dailyLimit")));
    }

    /** Absolute limit must be zero or negative (overdraft floor) — positive values should be rejected by @Max(0) */
    @Test
    void updateCustomerRejectsPositiveAbsoluteLimit() throws Exception {
        mockMvc.perform(put("/api/customers/" + pendingCustomerId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "absoluteLimit": 100.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("absoluteLimit")));
    }
}
