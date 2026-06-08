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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the TransactionController.
 * Tests the full HTTP cycle for transfers, deposits, withdrawals, and transaction history.
 * Verifies balance changes, role-based rules, ownership checks, and input validation.
 *
 * Uses two customers (Alice and Bob) with separate accounts to test both
 * own-account and cross-customer transfers.
 */
@SpringBootTest          // Boots full Spring context with real services and H2 database
@AutoConfigureMockMvc    // Provides MockMvc to simulate HTTP requests
@Transactional           // Rolls back database changes after each test for isolation
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    private String customerToken;   // JWT for Alice (customer1) — owns NL01FUNC0001 and NL02FUNC0001
    private String employeeToken;   // JWT for the seeded employee

    /**
     * Sets up two customers with accounts before each test:
     * - Alice: checking (NL01FUNC0001, €1000) + savings (NL02FUNC0001, €500)
     * - Bob: checking (NL03FUNC0001, €1000)
     * - Employee: uses the seeded employee@bank.com
     */
    @BeforeEach
    void setUp() {
        // Customer 1 (Alice) — has both checking and savings
        User customer1 = new User("Alice", "Test", "alice@functest.com",
                passwordEncoder.encode("pass"), "111111111", "0600000001", User.Role.CUSTOMER);
        customer1.setStatus(UserStatus.ACTIVE);
        customer1 = userRepository.save(customer1);

        accountRepository.save(new Account("NL01FUNC0001", AccountType.CHECKING,
                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer1));
        accountRepository.save(new Account("NL02FUNC0001", AccountType.SAVINGS,
                new BigDecimal("500.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer1));

        // Customer 2 (Bob) — has only checking
        User customer2 = new User("Bob", "Test", "bob@functest.com",
                passwordEncoder.encode("pass"), "222222222", "0600000002", User.Role.CUSTOMER);
        customer2.setStatus(UserStatus.ACTIVE);
        customer2 = userRepository.save(customer2);

        accountRepository.save(new Account("NL03FUNC0001", AccountType.CHECKING,
                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer2));

        // Load the seeded employee and generate tokens
        User employee = userRepository.findByEmail("employee@bank.com").orElseThrow();
        customerToken = jwtUtil.generateToken(customer1.getId(), customer1.getEmail());
        employeeToken = jwtUtil.generateToken(employee.getId(), employee.getEmail());
    }

    // ── Customer transfer with balance check ────────────────────

    /** Transfer between own accounts: checking balance should decrease and savings should increase */
    @Test
    void customerTransferDebitsSourceAndCreditsDestination() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "toIban": "NL02FUNC0001",
                                  "amount": 200.00,
                                  "description": "Transfer to savings"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("TRANSFER"))
                .andExpect(jsonPath("$.fromIban").value("NL01FUNC0001"))
                .andExpect(jsonPath("$.toIban").value("NL02FUNC0001"));

        // Use compareTo() instead of equals() because BigDecimal.equals() considers scale
        // (e.g. 800.00 != 800 with equals, but compareTo returns 0 meaning they are equal)
        assertEquals(0, new BigDecimal("800.00").compareTo(accountRepository.findById("NL01FUNC0001").orElseThrow().getBalance()));
        assertEquals(0, new BigDecimal("700.00").compareTo(accountRepository.findById("NL02FUNC0001").orElseThrow().getBalance()));
    }

    /** Customers can send money to another customer's checking account */
    @Test
    void customerTransferToOtherCustomerChecking() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "toIban": "NL03FUNC0001",
                                  "amount": 150.00,
                                  "description": "Payment"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("TRANSFER"));

        // Alice's checking decreased by 150, Bob's checking increased by 150
        assertEquals(0, new BigDecimal("850.00").compareTo(accountRepository.findById("NL01FUNC0001").orElseThrow().getBalance()));
        assertEquals(0, new BigDecimal("1150.00").compareTo(accountRepository.findById("NL03FUNC0001").orElseThrow().getBalance()));
    }

    // ── Deposit with balance check ────────────────────

    /** ATM deposit should increase the account balance (only toIban provided → inferred as DEPOSIT) */
    @Test
    void customerDepositCreditsAccount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "toIban": "NL01FUNC0001",
                                  "amount": 300.00,
                                  "description": "ATM deposit"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));

        assertEquals(0, new BigDecimal("1300.00").compareTo(accountRepository.findById("NL01FUNC0001").orElseThrow().getBalance()));
    }

    // ── Withdrawal with balance check ────────────────────

    /** ATM withdrawal should decrease the account balance (only fromIban provided → inferred as WITHDRAWAL) */
    @Test
    void customerWithdrawalDebitsAccount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "amount": 100.00,
                                  "description": "ATM withdrawal"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"));

        assertEquals(0, new BigDecimal("900.00").compareTo(accountRepository.findById("NL01FUNC0001").orElseThrow().getBalance()));
    }

    // ── Error handling ────────────────────

    /** Transferring more than the balance allows should fail (422) and leave the balance unchanged */
    @Test
    void transferReturns422WhenInsufficientFunds() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "toIban": "NL02FUNC0001",
                                  "amount": 5000.00,
                                  "description": "Too much"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());

        // Balance must remain unchanged after a failed transfer
        assertEquals(0, new BigDecimal("1000.00").compareTo(accountRepository.findById("NL01FUNC0001").orElseThrow().getBalance()));
    }

    /** Customers can only transfer from accounts they own — using someone else's account returns 403 */
    @Test
    void transferReturns403WhenCustomerDoesNotOwnAccount() throws Exception {
        // Alice tries to transfer from Bob's account (NL03FUNC0001) — should be denied
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL03FUNC0001",
                                  "toIban": "NL01FUNC0001",
                                  "amount": 100.00,
                                  "description": "Not my account"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    /** Transferring to a non-existent IBAN should return 404 */
    @Test
    void transferReturns404WhenAccountDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "toIban": "NL99NONEXIST",
                                  "amount": 100.00,
                                  "description": "No such account"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Employee transfer ────────────────────

    /** Employees can transfer between checking accounts of different customers */
    @Test
    void employeeTransferBetweenDifferentCustomers() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "toIban": "NL03FUNC0001",
                                  "amount": 100.00,
                                  "description": "Employee transfer"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("TRANSFER"));

        // Verify both balances changed correctly
        assertEquals(0, new BigDecimal("900.00").compareTo(accountRepository.findById("NL01FUNC0001").orElseThrow().getBalance()));
        assertEquals(0, new BigDecimal("1100.00").compareTo(accountRepository.findById("NL03FUNC0001").orElseThrow().getBalance()));
    }

    /** Deposits are ATM-only — employees must be blocked from performing them */
    @Test
    void employeeCannotDeposit() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "toIban": "NL01FUNC0001",
                                  "amount": 100.00,
                                  "description": "Deposit attempt"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    /** Withdrawals are ATM-only — employees must be blocked from performing them */
    @Test
    void employeeCannotWithdraw() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "amount": 100.00,
                                  "description": "Withdrawal attempt"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── GET /api/transactions ────────────────────

    /** Customers should only see transactions involving their own accounts */
    @Test
    void customerSeesOwnTransactionsOnly() throws Exception {
        // First create a transaction so there's something to query
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "toIban": "NL01FUNC0001",
                                  "amount": 50.00,
                                  "description": "Deposit"
                                }
                                """))
                .andExpect(status().isCreated());

        // Now query — should return a page with the customer's transactions
        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /** Employees should see all transactions across all customers */
    @Test
    void employeeSeesAllTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ── Validation rules ────────────────────

    /** Amount is required — omitting it should return 400 before reaching the service */
    @Test
    void rejectsMissingAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "toIban": "NL02FUNC0001"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    /** Zero is not a valid transaction amount — caught by @Positive validation */
    @Test
    void rejectsZeroAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "toIban": "NL02FUNC0001",
                                  "amount": 0.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    /** Negative amounts must be rejected to prevent reverse transfers */
    @Test
    void rejectsNegativeAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "toIban": "NL02FUNC0001",
                                  "amount": -50.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    /** Requests without a JWT token should be rejected by Spring Security (403 Forbidden) */
    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01FUNC0001",
                                  "toIban": "NL02FUNC0001",
                                  "amount": 100.00
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
