package com.banking.service;

import com.banking.dto.CustomerResponse;
import com.banking.dto.CustomerUpdateRequest;
import com.banking.exception.CustomerNotFoundException;
import com.banking.mapper.CustomerMapper;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private User pendingCustomer;
    private User activeCustomer;
    private CustomerResponse customerResponse;

    // Creates a pending customer, an active customer, and a response DTO.
    @BeforeEach
    void setUp() {
        pendingCustomer = new User("John", "Doe", "john@example.com", "pass",
                "123456789", "0612345678", User.Role.CUSTOMER);
        pendingCustomer.setId(1L);
        pendingCustomer.setStatus(UserStatus.PENDING);

        activeCustomer = new User("Jane", "Smith", "jane@example.com", "pass",
                "987654321", "0698765432", User.Role.CUSTOMER);
        activeCustomer.setId(2L);
        activeCustomer.setStatus(UserStatus.ACTIVE);

        customerResponse = new CustomerResponse(1L, "John", "Doe",
                "john@example.com", "123456789", "0612345678", "ACTIVE");
    }

    // ── Approve customer (PENDING → ACTIVE) ────────────────────

    // Approving activates the customer and creates their checking + savings accounts.
    @Test
    void approvePendingCustomerCreatesAccountsAndActivates() {
        CustomerUpdateRequest request = new CustomerUpdateRequest("ACTIVE",
                new BigDecimal("3000"), new BigDecimal("-100"));

        when(userRepository.findRequiredCustomerById(1L)).thenReturn(pendingCustomer);
        when(accountRepository.generateUniqueIban()).thenReturn("NL01TEST", "NL02TEST");
        when(customerMapper.toResponse(pendingCustomer)).thenReturn(customerResponse);

        customerService.updateCustomer(1L, request);

        assertEquals(UserStatus.ACTIVE, pendingCustomer.getStatus());
        verify(accountRepository).saveAll(any());
    }

    // Approving without limits uses sensible defaults.
    @Test
    void approveWithDefaultLimitsWhenNoneProvided() {
        CustomerUpdateRequest request = new CustomerUpdateRequest("ACTIVE", null, null);

        when(userRepository.findRequiredCustomerById(1L)).thenReturn(pendingCustomer);
        when(accountRepository.generateUniqueIban()).thenReturn("NL01TEST", "NL02TEST");
        when(customerMapper.toResponse(pendingCustomer)).thenReturn(customerResponse);

        customerService.updateCustomer(1L, request);

        assertEquals(UserStatus.ACTIVE, pendingCustomer.getStatus());
        verify(accountRepository).saveAll(any());
    }

    // ── Close customer ────────────────────

    // Closing sets status to CLOSED and deactivates all accounts.
    @Test
    void closeCustomerDeactivatesAccounts() {
        CustomerUpdateRequest request = new CustomerUpdateRequest("CLOSED", null, null);

        when(userRepository.findRequiredCustomerById(2L)).thenReturn(activeCustomer);
        when(customerMapper.toResponse(activeCustomer)).thenReturn(customerResponse);

        customerService.updateCustomer(2L, request);

        assertEquals(UserStatus.CLOSED, activeCustomer.getStatus());
        verify(accountRepository).updateActiveByUserId(2L, false);
    }

    // ── Reactivate closed customer ────────────────────

    // Reactivating restores ACTIVE status and re-enables all accounts.
    @Test
    void reactivateClosedCustomerActivatesAccounts() {
        activeCustomer.setStatus(UserStatus.CLOSED);
        CustomerUpdateRequest request = new CustomerUpdateRequest("ACTIVE", null, null);

        when(userRepository.findRequiredCustomerById(2L)).thenReturn(activeCustomer);
        when(customerMapper.toResponse(activeCustomer)).thenReturn(customerResponse);

        customerService.updateCustomer(2L, request);

        assertEquals(UserStatus.ACTIVE, activeCustomer.getStatus());
        verify(accountRepository).updateActiveByUserId(2L, true);
    }

    // ── Update limits ────────────────────

    // Updating limits without changing status only modifies account limits.
    @Test
    void updateLimitsCallsRepositoryUpdate() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(null,
                new BigDecimal("5000"), new BigDecimal("-200"));

        when(userRepository.findRequiredCustomerById(2L)).thenReturn(activeCustomer);
        when(customerMapper.toResponse(activeCustomer)).thenReturn(customerResponse);

        customerService.updateCustomer(2L, request);

        verify(accountRepository).updateLimitsByUserId(2L, new BigDecimal("5000"), new BigDecimal("-200"));
    }

    // ── Customer not found ────────────────────

    // No accounts are created when the customer doesn't exist.
    @Test
    void noAccountsCreatedWhenCustomerNotFound() {
        CustomerUpdateRequest request = new CustomerUpdateRequest("ACTIVE",
                new BigDecimal("2000"), BigDecimal.ZERO);

        when(userRepository.findRequiredCustomerById(99L))
                .thenThrow(new CustomerNotFoundException(99L));

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomer(99L, request));

        verify(accountRepository, never()).saveAll(any());
        verify(accountRepository, never()).updateActiveByUserId(anyLong(), anyBoolean());
    }

    // No limits are updated when the customer doesn't exist.
    @Test
    void noLimitsUpdatedWhenCustomerNotFound() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(null,
                new BigDecimal("5000"), new BigDecimal("-200"));

        when(userRepository.findRequiredCustomerById(99L))
                .thenThrow(new CustomerNotFoundException(99L));

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomer(99L, request));

        verify(accountRepository, never()).updateLimitsByUserId(anyLong(), any(), any());
    }
}
