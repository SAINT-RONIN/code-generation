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

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AccountMapper accountMapper;
    @Mock private AccountPolicy accountPolicy;

    @InjectMocks
    private AccountService accountService;

    private User activeCustomer;
    private Account checkingAccount;
    private AccountResponse accountResponse;

    // Creates a customer, checking account, and response DTO reused across tests.
    @BeforeEach
    void setUp() {
        activeCustomer = new User("Test", "Customer", "test@test.com", "pass", "123456789", "0600000000", User.Role.CUSTOMER);
        activeCustomer.setId(1L);
        activeCustomer.setStatus(UserStatus.ACTIVE);

        checkingAccount = new Account("NL01TEST", AccountType.CHECKING,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("2000"), activeCustomer);

        accountResponse = new AccountResponse("NL01TEST", "CHECKING", new BigDecimal("1000"),
                BigDecimal.ZERO, new BigDecimal("2000"), true,
                new AccountResponse.UserSummary(1L, "Test", "Customer", "test@test.com"));
    }

    // ── Create account ────────────────────

    // Account is saved and returned as a response DTO.
    @Test
    void createAccountSavesAndReturnsResponse() {
        AccountCreateRequest request = new AccountCreateRequest(1L, "CHECKING", new BigDecimal("2000"), BigDecimal.ZERO);

        when(userRepository.findRequiredCustomerById(1L)).thenReturn(activeCustomer);
        when(accountRepository.generateUniqueIban()).thenReturn("NL01TEST");
        when(accountRepository.save(any(Account.class))).thenReturn(checkingAccount);
        when(accountMapper.toResponse(checkingAccount)).thenReturn(accountResponse);

        AccountResponse result = accountService.createAccount(request);

        assertEquals("NL01TEST", result.iban());
        verify(accountPolicy).requireActiveCustomer(activeCustomer);
        verify(accountRepository).save(any(Account.class));
    }

    // ── Find my accounts ────────────────────

    // Returns mapped DTOs for the customer's active accounts.
    @Test
    void findMyAccountsReturnsMappedAccounts() {
        when(accountRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of(checkingAccount));
        when(accountMapper.toResponse(checkingAccount)).thenReturn(accountResponse);

        List<AccountResponse> result = accountService.findMyAccounts(1L);

        assertEquals(1, result.size());
        assertEquals("NL01TEST", result.get(0).iban());
    }

    // Returns an empty list when the customer has no accounts.
    @Test
    void findMyAccountsReturnsEmptyListWhenNoAccounts() {
        when(accountRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of());

        List<AccountResponse> result = accountService.findMyAccounts(1L);

        assertEquals(0, result.size());
    }

    // ── Update account ────────────────────

    // Setting active to false deactivates the account.
    @Test
    void updateAccountDeactivatesAccount() {
        AccountUpdateRequest request = new AccountUpdateRequest(false, null, null);

        when(accountRepository.findRequiredById("NL01TEST")).thenReturn(checkingAccount);
        when(accountMapper.toResponse(checkingAccount)).thenReturn(accountResponse);

        accountService.updateAccount("NL01TEST", request);

        assertFalse(checkingAccount.isActive());
    }

    // Partial update only changes the daily limit.
    @Test
    void updateAccountChangesDailyLimit() {
        AccountUpdateRequest request = new AccountUpdateRequest(null, new BigDecimal("5000"), null);

        when(accountRepository.findRequiredById("NL01TEST")).thenReturn(checkingAccount);
        when(accountMapper.toResponse(checkingAccount)).thenReturn(accountResponse);

        accountService.updateAccount("NL01TEST", request);

        assertEquals(new BigDecimal("5000"), checkingAccount.getDailyLimit());
    }

    // ── Policy rejection ────────────────────

    // Account is never saved when the customer is not active.
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
