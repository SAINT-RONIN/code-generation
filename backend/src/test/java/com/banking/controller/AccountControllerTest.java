package com.banking.controller;

import com.banking.model.Account;
import com.banking.model.Account.AccountType;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    private String employeeToken;
    private String customerToken;
    private Long customerId;

    // Creates a customer with a checking account and generates JWT tokens for both roles.
    @BeforeEach
    void setUp() {
        User customer = new User("Acc", "Tester", "acctest@test.com",
                passwordEncoder.encode("pass"), "777777777", "0600000007", User.Role.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
        customer = userRepository.save(customer);
        customerId = customer.getId();

        accountRepository.save(new Account("NL01ACC0001", AccountType.CHECKING,
                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer));

        User employee = userRepository.findByEmail("employee@bank.com").orElseThrow();
        customerToken = jwtUtil.generateToken(customer.getId(), customer.getEmail());
        employeeToken = jwtUtil.generateToken(employee.getId(), employee.getEmail());
    }

    // ── GET /api/accounts/me ────────────────────

    // Customer can retrieve their own accounts.
    @Test
    void getMyAccountsReturnsCustomerAccounts() throws Exception {
        mockMvc.perform(get("/api/accounts/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("NL01ACC0001"))
                .andExpect(jsonPath("$[0].accountType").value("CHECKING"));
    }

    // ── GET /api/accounts (employee) ────────────────────

    // Employee can list all accounts in the system.
    @Test
    void getAllAccountsReturnsPageForEmployee() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // Customer is denied access to the employee-only account list.
    @Test
    void getAllAccountsReturns403ForCustomer() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // ── POST /api/accounts (employee) ────────────────────

    // Employee can create a new account for an existing customer.
    @Test
    void createAccountReturns201OnSuccess() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "accountType": "SAVINGS",
                                  "dailyLimit": 2000.00,
                                  "absoluteLimit": 0.00
                                }
                                """.formatted(customerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.iban").isNotEmpty());
    }

    // Creating an account for a non-existent customer returns 404.
    @Test
    void createAccountReturns404WhenCustomerNotFound() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 99999,
                                  "accountType": "CHECKING",
                                  "dailyLimit": 2000.00,
                                  "absoluteLimit": 0.00
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── PUT /api/accounts/{iban} (employee) ────────────────────

    // Employee can deactivate an account and the change persists in the database.
    @Test
    void updateAccountDeactivatesAccount() throws Exception {
        mockMvc.perform(put("/api/accounts/NL01ACC0001")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "active": false }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value("NL01ACC0001"));

        assertFalse(accountRepository.findById("NL01ACC0001").orElseThrow().isActive());
    }

    // Updating a non-existent account returns 404.
    @Test
    void updateAccountReturns404WhenNotFound() throws Exception {
        mockMvc.perform(put("/api/accounts/NL99NONEXIST")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "active": false }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Validation ────────────────────

    // Missing customerId is rejected with 400.
    @Test
    void createAccountRejectsMissingCustomerId() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountType": "CHECKING",
                                  "dailyLimit": 2000.00,
                                  "absoluteLimit": 0.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("customerId")));
    }

    // Invalid account type is rejected with 400.
    @Test
    void createAccountRejectsInvalidAccountType() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "accountType": "INVALID",
                                  "dailyLimit": 2000.00,
                                  "absoluteLimit": 0.00
                                }
                                """.formatted(customerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("accountType")));
    }

    // Negative daily limit is rejected with 400.
    @Test
    void createAccountRejectsNegativeDailyLimit() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "accountType": "CHECKING",
                                  "dailyLimit": -500.00,
                                  "absoluteLimit": 0.00
                                }
                                """.formatted(customerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("dailyLimit")));
    }

    // Positive absolute limit is rejected with 400.
    @Test
    void createAccountRejectsPositiveAbsoluteLimit() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "accountType": "CHECKING",
                                  "dailyLimit": 2000.00,
                                  "absoluteLimit": 100.00
                                }
                                """.formatted(customerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("absoluteLimit")));
    }

    // Positive absolute limit on update is rejected with 400.
    @Test
    void updateAccountRejectsPositiveAbsoluteLimit() throws Exception {
        mockMvc.perform(put("/api/accounts/NL01ACC0001")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "absoluteLimit": 100.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("absoluteLimit")));
    }

    // Negative daily limit on update is rejected with 400.
    @Test
    void updateAccountRejectsNegativeDailyLimit() throws Exception {
        mockMvc.perform(put("/api/accounts/NL01ACC0001")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "dailyLimit": -100.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("dailyLimit")));
    }
}
