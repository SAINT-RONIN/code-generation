package com.banking.policy;

import com.banking.exception.InvalidTransferException;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for AccountPolicy.
 * Verifies that account creation is only allowed for ACTIVE customers
 * and blocked for PENDING and CLOSED ones.
 *
 * Pure unit tests — no Spring context, no mocks needed (policy is a plain POJO).
 */
class AccountPolicyTest {

    private AccountPolicy policy;

    @BeforeEach
    void setUp() {
        // AccountPolicy has no dependencies — just instantiate it directly
        policy = new AccountPolicy();
    }

    /** Active customers should be allowed to have accounts created for them */
    @Test
    void requireActiveCustomerPassesWhenActive() {
        User customer = buildCustomer(UserStatus.ACTIVE);
        assertDoesNotThrow(() -> policy.requireActiveCustomer(customer));
    }

    /** Pending customers haven't been approved yet — they should not get new accounts */
    @Test
    void requireActiveCustomerThrowsWhenPending() {
        User customer = buildCustomer(UserStatus.PENDING);
        assertThrows(InvalidTransferException.class,
                () -> policy.requireActiveCustomer(customer));
    }

    /** Closed customers have been deactivated — they should not get new accounts */
    @Test
    void requireActiveCustomerThrowsWhenClosed() {
        User customer = buildCustomer(UserStatus.CLOSED);
        assertThrows(InvalidTransferException.class,
                () -> policy.requireActiveCustomer(customer));
    }

    // Creates a User entity with the given status for testing
    private User buildCustomer(UserStatus status) {
        User user = new User("Test", "Customer", "test@test.com", "pass", "123456789", "0600000000", User.Role.CUSTOMER);
        user.setStatus(status);
        return user;
    }
}
