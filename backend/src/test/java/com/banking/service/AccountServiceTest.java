package com.banking.service;

import com.banking.dto.AccountCreateRequest;
import com.banking.dto.AccountResponse;
import com.banking.dto.AccountUpdateRequest;
import com.banking.exception.InvalidTransferException;
import com.banking.mapper.AccountMapper;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.policy.AccountPolicy;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AccountService using Mockito mocks.
 * Tests account creation, retrieval, and updates in isolation without a database.
 * Verifies that the service correctly delegates to repositories and the AccountPolicy.
 */
@ExtendWith(MockitoExtension.class) // Enables Mockito annotations without booting Spring
class AccountServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AccountMapper accountMapper;
    @Mock private AccountPolicy accountPolicy;

    @InjectMocks // Creates a real AccountService with the mocked dependencies
    private AccountService accountService;

    private User activeCustomer;
    private Account checkingAccount;
    private AccountResponse accountResponse;

    /**
     * Creates a customer, a checking account, and a matching response DTO
     * that are reused across all tests in this class.
     */
    @BeforeEach
    void setUp() {
        activeCustomer = new User("Test", "Customer", "test@test.com", "pass", "123456789", "0600000000", User.Role.CUSTOMER);
        activeCustomer.setId(1L);
        activeCustomer.setStatus(UserStatus.ACTIVE);

        // Account with €1000 balance, €0 absolute limit (no overdraft), €2000 daily limit
        checkingAccount = new Account("NL01TEST", AccountType.CHECKING,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("2000"), activeCustomer);

        accountResponse = new AccountResponse("NL01TEST", "CHECKING", new BigDecimal("1000"),
                BigDecimal.ZERO, new BigDecimal("2000"), true,
                new AccountResponse.UserSummary(1L, "Test", "Customer", "test@test.com"));
    }

    // ── Create account ────────────────────

    /** Verifies the full creation flow: find customer → check active status → generate IBAN → save → return DTO */
    @Test
    void createAccountSavesAndReturnsResponse() {
        AccountCreateRequest request = new AccountCreateRequest(1L, "CHECKING", new BigDecimal("2000"), BigDecimal.ZERO);

        when(userRepository.findRequiredCustomerById(1L)).thenReturn(activeCustomer);
        when(accountRepository.generateUniqueIban()).thenReturn("NL01TEST");
        when(accountRepository.save(any(Account.class))).thenReturn(checkingAccount);
        when(accountMapper.toResponse(checkingAccount)).thenReturn(accountResponse);

        AccountResponse result = accountService.createAccount(request);

        assertEquals("NL01TEST", result.iban());
        // Verify the policy was consulted to ensure the customer is ACTIVE
        verify(accountPolicy).requireActiveCustomer(activeCustomer);
        verify(accountRepository).save(any(Account.class));
    }

    // ── Find my accounts ────────────────────

    /** Ensures the service returns mapped DTOs for the customer's active accounts */
    @Test
    void findMyAccountsReturnsMappedAccounts() {
        when(accountRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of(checkingAccount));
        when(accountMapper.toResponse(checkingAccount)).thenReturn(accountResponse);

        List<AccountResponse> result = accountService.findMyAccounts(1L);

        assertEquals(1, result.size());
        assertEquals("NL01TEST", result.get(0).iban());
    }

    /** A customer with no accounts should get an empty list, not an error */
    @Test
    void findMyAccountsReturnsEmptyListWhenNoAccounts() {
        when(accountRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of());

        List<AccountResponse> result = accountService.findMyAccounts(1L);

        assertEquals(0, result.size());
    }

    // ── Update account ────────────────────

    /** Verifies that setting active=false actually calls deactivate() on the account entity */
    @Test
    void updateAccountDeactivatesAccount() {
        AccountUpdateRequest request = new AccountUpdateRequest(false, null, null);

        when(accountRepository.findRequiredById("NL01TEST")).thenReturn(checkingAccount);
        when(accountMapper.toResponse(checkingAccount)).thenReturn(accountResponse);

        accountService.updateAccount("NL01TEST", request);

        // The account entity should now be inactive
        assertFalse(checkingAccount.isActive());
    }

    /** Verifies that partial updates work — only the daily limit changes, other fields stay untouched */
    @Test
    void updateAccountChangesDailyLimit() {
        // Only dailyLimit is set — active and absoluteLimit should remain unchanged
        AccountUpdateRequest request = new AccountUpdateRequest(null, new BigDecimal("5000"), null);

        when(accountRepository.findRequiredById("NL01TEST")).thenReturn(checkingAccount);
        when(accountMapper.toResponse(checkingAccount)).thenReturn(accountResponse);

        accountService.updateAccount("NL01TEST", request);

        assertEquals(new BigDecimal("5000"), checkingAccount.getDailyLimit());
    }

    // ── Control flow: account not saved when policy rejects ────────────────────

    /** If the customer is not ACTIVE, the policy throws and the account must never be persisted */
    @Test
    void createAccountNotSavedWhenCustomerIsNotActive() {
        activeCustomer.setStatus(UserStatus.PENDING);
        AccountCreateRequest request = new AccountCreateRequest(1L, "CHECKING", new BigDecimal("2000"), BigDecimal.ZERO);

        when(userRepository.findRequiredCustomerById(1L)).thenReturn(activeCustomer);
        doThrow(new InvalidTransferException("Customer not active"))
                .when(accountPolicy).requireActiveCustomer(activeCustomer);

        assertThrows(InvalidTransferException.class, () -> accountService.createAccount(request));

        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository, never()).generateUniqueIban();
    }
}
