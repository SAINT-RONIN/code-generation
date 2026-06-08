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

/**
 * Integration tests for the AccountController.
 * Tests customer account viewing, employee account management (create, update, list),
 * role-based access control, and input validation on account operations.
 */
@SpringBootTest          // Boots full Spring context with real services and H2 database
@AutoConfigureMockMvc    // Provides MockMvc to simulate HTTP requests
@Transactional           // Rolls back database changes after each test
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    private String employeeToken;   // JWT for employee (can manage all accounts)
    private String customerToken;   // JWT for customer (can only view own accounts)
    private Long customerId;        // Customer ID for account creation tests

    /**
     * Creates an active customer with a checking account and generates JWT tokens
     * for both the customer and the seeded employee.
     */
    @BeforeEach
    void setUp() {
        // Create a customer with a known checking account for testing
        User customer = new User("Acc", "Tester", "acctest@test.com",
                passwordEncoder.encode("pass"), "777777777", "0600000007", User.Role.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
        customer = userRepository.save(customer);
        customerId = customer.getId();

        // Create a checking account with €1000 balance, €0 absolute limit, €2000 daily limit
        accountRepository.save(new Account("NL01ACC0001", AccountType.CHECKING,
                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer));

        // Load the seeded employee and generate tokens for both roles
        User employee = userRepository.findByEmail("employee@bank.com").orElseThrow();
        customerToken = jwtUtil.generateToken(customer.getId(), customer.getEmail());
        employeeToken = jwtUtil.generateToken(employee.getId(), employee.getEmail());
    }

    // ── GET /api/accounts/me ────────────────────

    /** Customers should see their own active accounts with correct IBAN and type */
    @Test
    void getMyAccountsReturnsCustomerAccounts() throws Exception {
        mockMvc.perform(get("/api/accounts/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("NL01ACC0001"))
                .andExpect(jsonPath("$[0].accountType").value("CHECKING"));
    }

    // ── GET /api/accounts (employee) ────────────────────

    /** Employees can browse all accounts in the system */
    @Test
    void getAllAccountsReturnsPageForEmployee() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /** Customers must not access the employee-only account list */
    @Test
    void getAllAccountsReturns403ForCustomer() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // ── POST /api/accounts (employee) ────────────────────

    /** Employees can create new accounts for existing customers */
    @Test
    void createAccountReturns201OnSuccess() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        // Use .formatted() to inject the dynamic customer ID into the JSON
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
                .andExpect(jsonPath("$.iban").isNotEmpty());  // IBAN is auto-generated
    }

    /** Creating an account for a non-existent customer should return 404 */
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

    /** Deactivating an account should persist the change in the database */
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

        // Verify the deactivation actually persisted to the database
        assertFalse(accountRepository.findById("NL01ACC0001").orElseThrow().isActive());
    }

    /** Updating a non-existent account should return 404 */
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

    // ── Validation rules ────────────────────

    /** customerId is required — omitting it should fail @NotNull validation */
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

    /** Only CHECKING and SAVINGS are valid — anything else should fail the @Pattern check */
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

    /** Daily limit must be zero or positive — negative values rejected by @Min(0) */
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

    /** Absolute limit must be zero or negative (overdraft floor) — positive values rejected by @Max(0) */
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

    /** Same absolute limit rule applies when updating an existing account */
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

    /** Same daily limit rule applies when updating an existing account */
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
