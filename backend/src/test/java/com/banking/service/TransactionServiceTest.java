package com.banking.service;

import com.banking.dto.TransactionRequest;
import com.banking.dto.TransactionResponse;
import com.banking.exception.InsufficientFundsException;
import com.banking.exception.InvalidTransferException;
import com.banking.exception.UnauthorizedAccountAccessException;
import com.banking.mapper.TransactionMapper;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.Transaction;
import com.banking.model.Transaction.TransactionType;
import com.banking.model.User;
import com.banking.policy.TransactionPolicy;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransactionService using Mockito mocks.
 * Tests all transaction types (transfer, deposit, withdrawal) and verifies:
 * - Balance changes on source and destination accounts
 * - Policy enforcement (ownership, transfer rules, limits)
 * - Error handling (insufficient funds, unauthorized access)
 * - Employee restrictions (no deposits/withdrawals)
 * - That failed transactions never record or move money
 */
@ExtendWith(MockitoExtension.class) // Enables Mockito annotations without booting Spring
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionMapper transactionMapper;
    @Mock private TransactionPolicy transactionPolicy;

    @InjectMocks // Creates a real TransactionService with the mocked dependencies
    private TransactionService transactionService;

    private User customer;
    private Account checkingAccount;   // NL01TEST — €1000 balance
    private Account savingsAccount;    // NL02TEST — €500 balance

    /**
     * Creates a customer with a checking (€1000) and savings (€500) account.
     * These are real Account objects (not mocks) so we can verify balance changes.
     */
    @BeforeEach
    void setUp() {
        customer = new User("Test", "Customer", "test@test.com", "pass", "123456789", "0600000000", User.Role.CUSTOMER);
        customer.setId(1L);

        checkingAccount = new Account("NL01TEST", AccountType.CHECKING,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("2000"), customer);

        savingsAccount = new Account("NL02TEST", AccountType.SAVINGS,
                new BigDecimal("500"), BigDecimal.ZERO, new BigDecimal("2000"), customer);
    }

    // ── Customer transfer ────────────────────

    /** Verifies the happy path: source loses money, destination gains money, and all policies are checked */
    @Test
    void customerTransferDebitsSourceCreditsDestinationAndValidatesPolicy() {
        TransactionRequest request = new TransactionRequest("NL01TEST", "NL02TEST",
                new BigDecimal("200"), "Payment");
        stubTransferHappyPath("NL01TEST", "NL02TEST", new BigDecimal("200"), "test@test.com", "Payment");

        TransactionResponse result = transactionService.createCustomerTransaction(request, 1L, "test@test.com");

        // Verify balance changes: 1000 - 200 = 800, 500 + 200 = 700
        assertEquals(new BigDecimal("800"), checkingAccount.getBalance());
        assertEquals(new BigDecimal("700"), savingsAccount.getBalance());
        assertEquals("TRANSFER", result.transactionType());
        // Verify that ownership and transfer rules were enforced
        verify(transactionPolicy).requireCallerOwnsAccount(checkingAccount, 1L);
        verify(transactionPolicy).validateCustomerTransfer(checkingAccount, savingsAccount);
    }

    // ── Employee transfer ────────────────────

    /** Verifies employees can transfer between different customers and that the employee-specific policy is enforced */
    @Test
    void employeeTransferDebitsSourceCreditsDestinationAndValidatesPolicy() {
        // Create a second customer with their own checking account
        User customer2 = new User("Other", "Customer", "other@test.com", "pass", "987654321", "0600000001", User.Role.CUSTOMER);
        customer2.setId(2L);
        Account customer2Checking = new Account("NL03TEST", AccountType.CHECKING,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("2000"), customer2);

        TransactionRequest request = new TransactionRequest("NL01TEST", "NL03TEST",
                new BigDecimal("100"), "Note");
        Transaction savedTx = new Transaction("NL01TEST", "NL03TEST", new BigDecimal("100"),
                "employee@bank.com", "Note", TransactionType.TRANSFER);
        TransactionResponse response = new TransactionResponse(1L, "NL01TEST", "NL03TEST",
                new BigDecimal("100"), LocalDateTime.now(), "employee@bank.com", "Note", "TRANSFER");

        when(accountRepository.findRequiredActiveById("NL01TEST")).thenReturn(checkingAccount);
        when(accountRepository.findRequiredActiveById("NL03TEST")).thenReturn(customer2Checking);
        when(transactionRepository.sumOutgoingTodayForIban("NL01TEST")).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.record(any(), any(), any(), any(), any(), any())).thenReturn(savedTx);
        when(transactionMapper.toResponse(savedTx)).thenReturn(response);

        transactionService.createEmployeeTransfer(request, "employee@bank.com");

        // Verify balance changes: 1000 - 100 = 900, 1000 + 100 = 1100
        assertEquals(new BigDecimal("900"), checkingAccount.getBalance());
        assertEquals(new BigDecimal("1100"), customer2Checking.getBalance());
        // Verify the employee-specific policy was enforced (checking-to-checking, different customers)
        verify(transactionPolicy).validateEmployeeTransfer(checkingAccount, customer2Checking);
    }

    /** Deposits are ATM-only operations — employees must not be able to perform them */
    @Test
    void employeeCannotDeposit() {
        // Only toIban → inferred as DEPOSIT → should be rejected for employees
        TransactionRequest request = new TransactionRequest(null, "NL01TEST",
                new BigDecimal("100"), "ATM deposit");

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> transactionService.createEmployeeTransfer(request, "employee@bank.com"));
    }

    /** Withdrawals are ATM-only operations — employees must not be able to perform them */
    @Test
    void employeeCannotWithdraw() {
        // Only fromIban → inferred as WITHDRAWAL → should be rejected for employees
        TransactionRequest request = new TransactionRequest("NL01TEST", null,
                new BigDecimal("100"), "ATM withdrawal");

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> transactionService.createEmployeeTransfer(request, "employee@bank.com"));
    }

    // ── Deposit ────────────────────

    /** Verifies that a deposit (only toIban provided) adds money to the account */
    @Test
    void depositCreditsAccountBalance() {
        TransactionRequest request = new TransactionRequest(null, "NL01TEST",
                new BigDecimal("300"), "ATM deposit");
        Transaction savedTx = new Transaction(null, "NL01TEST", new BigDecimal("300"),
                "test@test.com", "ATM deposit", TransactionType.DEPOSIT);
        TransactionResponse response = new TransactionResponse(1L, null, "NL01TEST",
                new BigDecimal("300"), LocalDateTime.now(), "test@test.com", "ATM deposit", "DEPOSIT");

        when(accountRepository.findRequiredActiveById("NL01TEST")).thenReturn(checkingAccount);
        when(transactionRepository.record(eq(null), eq("NL01TEST"), any(), any(), any(), eq(TransactionType.DEPOSIT))).thenReturn(savedTx);
        when(transactionMapper.toResponse(savedTx)).thenReturn(response);

        TransactionResponse result = transactionService.createCustomerTransaction(request, 1L, "test@test.com");

        // 1000 + 300 = 1300
        assertEquals(new BigDecimal("1300"), checkingAccount.getBalance());
        assertEquals("DEPOSIT", result.transactionType());
    }

    // ── Withdrawal ────────────────────

    /** Verifies that a withdrawal (only fromIban provided) removes money from the account */
    @Test
    void withdrawalDebitsAccountBalance() {
        TransactionRequest request = new TransactionRequest("NL01TEST", null,
                new BigDecimal("100"), "ATM withdrawal");
        Transaction savedTx = new Transaction("NL01TEST", null, new BigDecimal("100"),
                "test@test.com", "ATM withdrawal", TransactionType.WITHDRAWAL);
        TransactionResponse response = new TransactionResponse(1L, "NL01TEST", null,
                new BigDecimal("100"), LocalDateTime.now(), "test@test.com", "ATM withdrawal", "WITHDRAWAL");

        when(accountRepository.findRequiredActiveById("NL01TEST")).thenReturn(checkingAccount);
        when(transactionRepository.sumOutgoingTodayForIban("NL01TEST")).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.record(eq("NL01TEST"), eq(null), any(), any(), any(), eq(TransactionType.WITHDRAWAL))).thenReturn(savedTx);
        when(transactionMapper.toResponse(savedTx)).thenReturn(response);

        TransactionResponse result = transactionService.createCustomerTransaction(request, 1L, "test@test.com");

        // 1000 - 100 = 900
        assertEquals(new BigDecimal("900"), checkingAccount.getBalance());
        assertEquals("WITHDRAWAL", result.transactionType());
    }

    // ── Invalid input ────────────────────

    /** The service infers type from IBANs — if neither is provided, it cannot determine the transaction type */
    @Test
    void throwsWhenBothIbansAreBlank() {
        TransactionRequest request = new TransactionRequest("", "",
                new BigDecimal("100"), "Note");

        assertThrows(InvalidTransferException.class,
                () -> transactionService.createCustomerTransaction(request, 1L, "test@test.com"));
    }

    // ── Control flow: when a policy check fails, the transaction is never recorded ────────────────────
    // These tests verify that money never moves and no transaction is saved when a policy check rejects.

    /** If the caller doesn't own the source account, the flow must stop before recording or moving money */
    @Test
    void transferNotRecordedWhenCallerDoesNotOwnAccount() {
        TransactionRequest request = new TransactionRequest("NL01TEST", "NL02TEST",
                new BigDecimal("200"), "Payment");

        when(accountRepository.findRequiredActiveById("NL01TEST")).thenReturn(checkingAccount);
        when(accountRepository.findRequiredActiveById("NL02TEST")).thenReturn(savingsAccount);
        // Simulate the policy rejecting because user 99 doesn't own the account
        doThrow(new UnauthorizedAccountAccessException("Not your account"))
                .when(transactionPolicy).requireCallerOwnsAccount(checkingAccount, 99L);

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> transactionService.createCustomerTransaction(request, 99L, "intruder@test.com"));

        // No transaction should be recorded and balances must remain unchanged
        verify(transactionRepository, never()).record(any(), any(), any(), any(), any(), any());
        assertEquals(new BigDecimal("1000"), checkingAccount.getBalance());
    }

    /** If the amount exceeds the absolute limit, no money should move and no transaction should be saved */
    @Test
    void transferNotRecordedWhenInsufficientFunds() {
        TransactionRequest request = new TransactionRequest("NL01TEST", "NL02TEST",
                new BigDecimal("5000"), "Too much");

        when(accountRepository.findRequiredActiveById("NL01TEST")).thenReturn(checkingAccount);
        when(accountRepository.findRequiredActiveById("NL02TEST")).thenReturn(savingsAccount);
        // Simulate the policy rejecting because 5000 would breach the absolute limit
        doThrow(new InsufficientFundsException("NL01TEST"))
                .when(transactionPolicy).enforceAbsoluteLimit(checkingAccount, new BigDecimal("5000"));

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.createCustomerTransaction(request, 1L, "test@test.com"));

        // Both balances must remain unchanged
        verify(transactionRepository, never()).record(any(), any(), any(), any(), any(), any());
        assertEquals(new BigDecimal("1000"), checkingAccount.getBalance());
        assertEquals(new BigDecimal("500"), savingsAccount.getBalance());
    }

    /** If the employee policy rejects the transfer, no money should move and no transaction should be saved */
    @Test
    void employeeTransferNotRecordedWhenPolicyRejects() {
        TransactionRequest request = new TransactionRequest("NL01TEST", "NL02TEST",
                new BigDecimal("100"), "Note");

        when(accountRepository.findRequiredActiveById("NL01TEST")).thenReturn(checkingAccount);
        when(accountRepository.findRequiredActiveById("NL02TEST")).thenReturn(savingsAccount);
        // Simulate the policy rejecting (e.g. savings account involved in employee transfer)
        doThrow(new InvalidTransferException("Employee transfers must be between checking accounts"))
                .when(transactionPolicy).validateEmployeeTransfer(checkingAccount, savingsAccount);

        assertThrows(InvalidTransferException.class,
                () -> transactionService.createEmployeeTransfer(request, "employee@bank.com"));

        // No transaction recorded, balance unchanged
        verify(transactionRepository, never()).record(any(), any(), any(), any(), any(), any());
        assertEquals(new BigDecimal("1000"), checkingAccount.getBalance());
    }

    // ── Helpers ────────────────────

    /**
     * Sets up all the mocks needed for a successful transfer test.
     * Stubs account lookups, daily limit check (returns zero spent today),
     * transaction recording, and response mapping.
     */
    private void stubTransferHappyPath(String fromIban, String toIban, BigDecimal amount,
                                       String performedBy, String description) {
        Transaction savedTx = new Transaction(fromIban, toIban, amount,
                performedBy, description, TransactionType.TRANSFER);
        TransactionResponse response = new TransactionResponse(1L, fromIban, toIban,
                amount, LocalDateTime.now(), performedBy, description, "TRANSFER");

        when(accountRepository.findRequiredActiveById(fromIban)).thenReturn(checkingAccount);
        when(accountRepository.findRequiredActiveById(toIban)).thenReturn(savingsAccount);
        // Return zero for today's spending so the daily limit check passes
        when(transactionRepository.sumOutgoingTodayForIban(fromIban)).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.record(any(), any(), any(), any(), any(), any())).thenReturn(savedTx);
        when(transactionMapper.toResponse(savedTx)).thenReturn(response);
    }
}
