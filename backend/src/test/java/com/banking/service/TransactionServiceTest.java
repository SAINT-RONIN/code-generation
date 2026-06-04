package com.banking.service;

import com.banking.dto.TransactionRequest;
import com.banking.dto.TransactionResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionMapper transactionMapper;
    @Mock private TransactionPolicy transactionPolicy;

    @InjectMocks
    private TransactionService transactionService;

    private User customer;
    private Account checkingAccount;
    private Account savingsAccount;

    @BeforeEach
    void setUp() {
        customer = new User("Test", "Customer", "test@test.com", "pass", "123456789", "0600000000", User.Role.CUSTOMER);
        customer.setId(1L);

        checkingAccount = new Account("NL01TEST", AccountType.CHECKING,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("2000"), customer);

        savingsAccount = new Account("NL02TEST", AccountType.SAVINGS,
                new BigDecimal("500"), BigDecimal.ZERO, new BigDecimal("2000"), customer);
    }

    // ── Transfer ────────────────────

    @Test
    void transferCreditsDestinationAndDebitsSource() {
        TransactionRequest request = new TransactionRequest("NL01TEST", "NL02TEST",
                new BigDecimal("200"), "Payment");
        Transaction savedTx = new Transaction("NL01TEST", "NL02TEST", new BigDecimal("200"),
                "test@test.com", "Payment", TransactionType.TRANSFER);
        TransactionResponse response = new TransactionResponse(1L, "NL01TEST", "NL02TEST",
                new BigDecimal("200"), LocalDateTime.now(), "test@test.com", "Payment", "TRANSFER");

        when(accountRepository.findRequiredActiveById("NL01TEST")).thenReturn(checkingAccount);
        when(accountRepository.findRequiredActiveById("NL02TEST")).thenReturn(savingsAccount);
        when(transactionRepository.sumOutgoingTodayForIban("NL01TEST")).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.record(any(), any(), any(), any(), any(), any())).thenReturn(savedTx);
        when(transactionMapper.toResponse(savedTx)).thenReturn(response);

        TransactionResponse result = transactionService.createTransaction(request, 1L, "test@test.com", false);

        assertEquals(new BigDecimal("800"), checkingAccount.getBalance());
        assertEquals(new BigDecimal("700"), savingsAccount.getBalance());
        assertEquals("TRANSFER", result.transactionType());
    }

    @Test
    void throwsWhenBothIbansAreBlank() {
        TransactionRequest request = new TransactionRequest("", "",
                new BigDecimal("100"), "Note");

        assertThrows(InvalidTransferException.class,
                () -> transactionService.createTransaction(request, 1L, "test@test.com", false));
    }

    @Test
    void depositRejectedForEmployee() {
        TransactionRequest request = new TransactionRequest(null, "NL01TEST",
                new BigDecimal("100"), "ATM deposit");

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> transactionService.createTransaction(request, 99L, "employee@bank.com", true));
    }

    @Test
    void withdrawalRejectedForEmployee() {
        TransactionRequest request = new TransactionRequest("NL01TEST", null,
                new BigDecimal("100"), "ATM withdrawal");

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> transactionService.createTransaction(request, 99L, "employee@bank.com", true));
    }

    @Test
    void transferCallsEmployeePolicyWhenEmployee() {
        TransactionRequest request = new TransactionRequest("NL01TEST", "NL02TEST",
                new BigDecimal("100"), "Note");
        Transaction savedTx = new Transaction("NL01TEST", "NL02TEST", new BigDecimal("100"),
                "employee@bank.com", "Note", TransactionType.TRANSFER);
        TransactionResponse response = new TransactionResponse(1L, "NL01TEST", "NL02TEST",
                new BigDecimal("100"), LocalDateTime.now(), "employee@bank.com", "Note", "TRANSFER");

        when(accountRepository.findRequiredActiveById("NL01TEST")).thenReturn(checkingAccount);
        when(accountRepository.findRequiredActiveById("NL02TEST")).thenReturn(savingsAccount);
        when(transactionRepository.sumOutgoingTodayForIban("NL01TEST")).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.record(any(), any(), any(), any(), any(), any())).thenReturn(savedTx);
        when(transactionMapper.toResponse(savedTx)).thenReturn(response);

        transactionService.createTransaction(request, 99L, "employee@bank.com", true);

        verify(transactionPolicy).validateEmployeeTransfer(checkingAccount, savingsAccount);
    }

    @Test
    void transferCallsCustomerPolicyWhenCustomer() {
        TransactionRequest request = new TransactionRequest("NL01TEST", "NL02TEST",
                new BigDecimal("100"), "Note");
        Transaction savedTx = new Transaction("NL01TEST", "NL02TEST", new BigDecimal("100"),
                "test@test.com", "Note", TransactionType.TRANSFER);
        TransactionResponse response = new TransactionResponse(1L, "NL01TEST", "NL02TEST",
                new BigDecimal("100"), LocalDateTime.now(), "test@test.com", "Note", "TRANSFER");

        when(accountRepository.findRequiredActiveById("NL01TEST")).thenReturn(checkingAccount);
        when(accountRepository.findRequiredActiveById("NL02TEST")).thenReturn(savingsAccount);
        when(transactionRepository.sumOutgoingTodayForIban("NL01TEST")).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.record(any(), any(), any(), any(), any(), any())).thenReturn(savedTx);
        when(transactionMapper.toResponse(savedTx)).thenReturn(response);

        transactionService.createTransaction(request, 1L, "test@test.com", false);

        verify(transactionPolicy).requireCallerOwnsAccount(checkingAccount, 1L);
        verify(transactionPolicy).validateCustomerTransfer(checkingAccount, savingsAccount);
    }

    // ── Deposit ────────────────────

    @Test
    void depositCreditsAccount() {
        TransactionRequest request = new TransactionRequest(null, "NL01TEST",
                new BigDecimal("300"), "ATM deposit");
        Transaction savedTx = new Transaction(null, "NL01TEST", new BigDecimal("300"),
                "test@test.com", "ATM deposit", TransactionType.DEPOSIT);
        TransactionResponse response = new TransactionResponse(1L, null, "NL01TEST",
                new BigDecimal("300"), LocalDateTime.now(), "test@test.com", "ATM deposit", "DEPOSIT");

        when(accountRepository.findRequiredActiveById("NL01TEST")).thenReturn(checkingAccount);
        when(transactionRepository.record(eq(null), eq("NL01TEST"), any(), any(), any(), eq(TransactionType.DEPOSIT))).thenReturn(savedTx);
        when(transactionMapper.toResponse(savedTx)).thenReturn(response);

        TransactionResponse result = transactionService.createTransaction(request, 1L, "test@test.com", false);

        assertEquals(new BigDecimal("1300"), checkingAccount.getBalance());
        assertEquals("DEPOSIT", result.transactionType());
    }

    // ── Withdrawal ────────────────────

    @Test
    void withdrawalDebitsAccount() {
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

        TransactionResponse result = transactionService.createTransaction(request, 1L, "test@test.com", false);

        assertEquals(new BigDecimal("900"), checkingAccount.getBalance());
        assertEquals("WITHDRAWAL", result.transactionType());
    }

}
