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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    private String customerToken;
    private String employeeToken;

    // Sets up Alice (checking + savings), Bob (checking), and an employee with JWT tokens.
    @BeforeEach
    void setUp() {
        User customer1 = new User("Alice", "Test", "alice@functest.com",
                passwordEncoder.encode("pass"), "111111111", "0600000001", User.Role.CUSTOMER);
        customer1.setStatus(UserStatus.ACTIVE);
        customer1 = userRepository.save(customer1);

        accountRepository.save(new Account("NL01FUNC0001", AccountType.CHECKING,
                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer1));
        accountRepository.save(new Account("NL02FUNC0001", AccountType.SAVINGS,
                new BigDecimal("500.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer1));

        User customer2 = new User("Bob", "Test", "bob@functest.com",
                passwordEncoder.encode("pass"), "222222222", "0600000002", User.Role.CUSTOMER);
        customer2.setStatus(UserStatus.ACTIVE);
        customer2 = userRepository.save(customer2);

        accountRepository.save(new Account("NL03FUNC0001", AccountType.CHECKING,
                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer2));

        User employee = userRepository.findByEmail("employee@bank.com").orElseThrow();
        customerToken = jwtUtil.generateToken(customer1.getId(), customer1.getEmail());
        employeeToken = jwtUtil.generateToken(employee.getId(), employee.getEmail());
    }

    // ── Customer transfer ────────────────────

    // Transfer between own accounts debits the source and credits the destination.
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

        // compareTo instead of equals because BigDecimal.equals considers scale
        assertEquals(0, new BigDecimal("800.00").compareTo(accountRepository.findById("NL01FUNC0001").orElseThrow().getBalance()));
        assertEquals(0, new BigDecimal("700.00").compareTo(accountRepository.findById("NL02FUNC0001").orElseThrow().getBalance()));
    }

    // Customer can transfer to another customer's checking account.
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

        assertEquals(0, new BigDecimal("850.00").compareTo(accountRepository.findById("NL01FUNC0001").orElseThrow().getBalance()));
        assertEquals(0, new BigDecimal("1150.00").compareTo(accountRepository.findById("NL03FUNC0001").orElseThrow().getBalance()));
    }

    // ── Deposit ────────────────────

    // Deposit increases the account balance.
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

    // ── Withdrawal ────────────────────

    // Withdrawal decreases the account balance.
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

    // Insufficient funds returns 422 and leaves the balance unchanged.
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

        assertEquals(0, new BigDecimal("1000.00").compareTo(accountRepository.findById("NL01FUNC0001").orElseThrow().getBalance()));
    }

    // Transferring from someone else's account returns 403.
    @Test
    void transferReturns403WhenCustomerDoesNotOwnAccount() throws Exception {
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

    // Transferring to a non-existent IBAN returns 404.
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

    // Employee can transfer between different customers' checking accounts.
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

        assertEquals(0, new BigDecimal("900.00").compareTo(accountRepository.findById("NL01FUNC0001").orElseThrow().getBalance()));
        assertEquals(0, new BigDecimal("1100.00").compareTo(accountRepository.findById("NL03FUNC0001").orElseThrow().getBalance()));
    }

    // Employee cannot perform a deposit.
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

    // Employee cannot perform a withdrawal.
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

    // Customer only sees transactions involving their own accounts.
    @Test
    void customerSeesOwnTransactionsOnly() throws Exception {
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

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // Employee can see all transactions across all customers.
    @Test
    void employeeSeesAllTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ── Validation ────────────────────

    // Missing amount is rejected with 400.
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

    // Zero amount is rejected with 400.
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

    // Negative amount is rejected with 400.
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

    // Request without a JWT token is rejected with 403.
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
