package com.banking.policy;

import com.banking.exception.InvalidTransferException;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountPolicyTest {

    private AccountPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AccountPolicy();
    }

    @Test
    void requireActiveCustomerPassesWhenActive() {
        User customer = buildCustomer(UserStatus.ACTIVE);
        assertDoesNotThrow(() -> policy.requireActiveCustomer(customer));
    }

    @Test
    void requireActiveCustomerThrowsWhenPending() {
        User customer = buildCustomer(UserStatus.PENDING);
        assertThrows(InvalidTransferException.class,
                () -> policy.requireActiveCustomer(customer));
    }

    @Test
    void requireActiveCustomerThrowsWhenClosed() {
        User customer = buildCustomer(UserStatus.CLOSED);
        assertThrows(InvalidTransferException.class,
                () -> policy.requireActiveCustomer(customer));
    }

    private User buildCustomer(UserStatus status) {
        User user = new User("Test", "Customer", "test@test.com", "pass", "123456789", "0600000000", User.Role.CUSTOMER);
        user.setStatus(status);
        return user;
    }
}
