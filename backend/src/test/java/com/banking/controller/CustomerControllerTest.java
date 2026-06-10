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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    private String employeeToken;
    private String customerToken;
    private Long pendingCustomerId;

    // Creates a pending customer, an active customer, and generates tokens for both roles.
    @BeforeEach
    void setUp() {
        User pending = new User("Pending", "Customer", "pendingcust@test.com",
                passwordEncoder.encode("pass"), "888888888", "0600000008", User.Role.CUSTOMER);
        pending.setStatus(UserStatus.PENDING);
        pending = userRepository.save(pending);
        pendingCustomerId = pending.getId();

        User employee = userRepository.findByEmail("employee@bank.com").orElseThrow();
        employeeToken = jwtUtil.generateToken(employee.getId(), employee.getEmail());

        User customer = new User("Active", "Customer", "activecust@test.com",
                passwordEncoder.encode("pass"), "999999999", "0600000009", User.Role.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
        customer = userRepository.save(customer);
        customerToken = jwtUtil.generateToken(customer.getId(), customer.getEmail());
    }

    // ── GET /api/customers (employee only) ────────────────────

    // Employee can list all customers.
    @Test
    void getCustomersReturnsPageForEmployee() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // Customer is denied access to the customer list.
    @Test
    void getCustomersReturns403ForCustomer() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/customers/{id} — approve ────────────────────

    // Approving a pending customer sets them to ACTIVE and creates their bank accounts.
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

        assertEquals(UserStatus.ACTIVE, userRepository.findById(pendingCustomerId).orElseThrow().getStatus());
        assertFalse(accountRepository.findByUserIdAndActiveTrue(pendingCustomerId).isEmpty());
    }

    // ── PUT /api/customers/{id} — close ────────────────────

    // Closing a customer sets their status to CLOSED.
    @Test
    void closeCustomerDeactivatesAccounts() throws Exception {
        mockMvc.perform(put("/api/customers/" + pendingCustomerId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "ACTIVE", "dailyLimit": 2000.00, "absoluteLimit": 0.00 }
                                """))
                .andExpect(status().isOk());

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

    // Updating a non-existent customer returns 404.
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

    // ── Validation ────────────────────

    // Negative daily limit is rejected with 400.
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

    // Positive absolute limit is rejected with 400.
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
