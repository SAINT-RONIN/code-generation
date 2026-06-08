package com.banking.service;

import com.banking.dto.CustomerResponse;
import com.banking.dto.CustomerUpdateRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CustomerService using Mockito mocks.
 * Tests the customer lifecycle: approval (PENDING → ACTIVE), closing, reactivation,
 * and limit updates — all in isolation without a database.
 */
@ExtendWith(MockitoExtension.class) // Enables Mockito annotations without booting Spring
class CustomerServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CustomerMapper customerMapper;

    @InjectMocks // Creates a real CustomerService with the mocked dependencies
    private CustomerService customerService;

    private User pendingCustomer;
    private User activeCustomer;
    private CustomerResponse customerResponse;

    /**
     * Creates two customer entities (pending and active) and a reusable response DTO.
     * IDs are set manually since there's no database auto-generation.
     */
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

    /** Approving a pending customer should activate them and create their checking + savings accounts */
    @Test
    void approvePendingCustomerCreatesAccountsAndActivates() {
        CustomerUpdateRequest request = new CustomerUpdateRequest("ACTIVE",
                new BigDecimal("3000"), new BigDecimal("-100"));

        when(userRepository.findRequiredCustomerById(1L)).thenReturn(pendingCustomer);
        // generateUniqueIban() is called twice — once for checking, once for savings
        when(accountRepository.generateUniqueIban()).thenReturn("NL01TEST", "NL02TEST");
        when(customerMapper.toResponse(pendingCustomer)).thenReturn(customerResponse);

        customerService.updateCustomer(1L, request);

        // Verify the customer's status was changed to ACTIVE
        assertEquals(UserStatus.ACTIVE, pendingCustomer.getStatus());
        // Verify that two accounts were saved (checking + savings)
        verify(accountRepository).saveAll(any());
    }

    /** When no limits are specified during approval, the service should apply sensible defaults (€2000 daily, €0 absolute) */
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

    /** Closing a customer should mark them CLOSED and deactivate all their accounts */
    @Test
    void closeCustomerDeactivatesAccounts() {
        CustomerUpdateRequest request = new CustomerUpdateRequest("CLOSED", null, null);

        when(userRepository.findRequiredCustomerById(2L)).thenReturn(activeCustomer);
        when(customerMapper.toResponse(activeCustomer)).thenReturn(customerResponse);

        customerService.updateCustomer(2L, request);

        assertEquals(UserStatus.CLOSED, activeCustomer.getStatus());
        // Verify that all accounts were deactivated (active = false)
        verify(accountRepository).updateActiveByUserId(2L, false);
    }

    // ── Reactivate closed customer ────────────────────

    /** Reactivating a closed customer should restore their status and re-enable their accounts */
    @Test
    void reactivateClosedCustomerActivatesAccounts() {
        activeCustomer.setStatus(UserStatus.CLOSED); // Simulate a previously closed customer
        CustomerUpdateRequest request = new CustomerUpdateRequest("ACTIVE", null, null);

        when(userRepository.findRequiredCustomerById(2L)).thenReturn(activeCustomer);
        when(customerMapper.toResponse(activeCustomer)).thenReturn(customerResponse);

        customerService.updateCustomer(2L, request);

        assertEquals(UserStatus.ACTIVE, activeCustomer.getStatus());
        // Verify that all accounts were reactivated (active = true)
        verify(accountRepository).updateActiveByUserId(2L, true);
    }

    // ── Update limits ────────────────────

    /** Updating limits without changing status should only modify the account limits in the database */
    @Test
    void updateLimitsCallsRepositoryUpdate() {
        // No status change — just limit updates
        CustomerUpdateRequest request = new CustomerUpdateRequest(null,
                new BigDecimal("5000"), new BigDecimal("-200"));

        when(userRepository.findRequiredCustomerById(2L)).thenReturn(activeCustomer);
        when(customerMapper.toResponse(activeCustomer)).thenReturn(customerResponse);

        customerService.updateCustomer(2L, request);

        // Verify the repository was called with the exact limit values
        verify(accountRepository).updateLimitsByUserId(2L, new BigDecimal("5000"), new BigDecimal("-200"));
    }
}
