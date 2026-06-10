package com.banking.model;

import com.banking.model.User.Role;
import com.banking.model.User.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    // Constructor stores all provided fields correctly.
    @Test
    void constructorSetsAllFields() {
        User user = new User("John", "Doe", "john@test.com", "hashed-pass",
                "123456789", "0612345678", Role.CUSTOMER);

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals("hashed-pass", user.getPassword());
        assertEquals("123456789", user.getBsn());
        assertEquals("0612345678", user.getPhoneNumber());
        assertEquals(Role.CUSTOMER, user.getRole());
    }

    // New users default to PENDING status.
    @Test
    void newUserDefaultsToPendingStatus() {
        User user = new User("Jane", "Smith", "jane@test.com", "pass",
                "987654321", "0698765432", Role.CUSTOMER);

        assertEquals(UserStatus.PENDING, user.getStatus());
    }

    // New users start with an empty accounts list.
    @Test
    void newUserHasEmptyAccountsList() {
        User user = new User("Jane", "Smith", "jane@test.com", "pass",
                "987654321", "0698765432", Role.CUSTOMER);

        assertNotNull(user.getAccounts());
        assertTrue(user.getAccounts().isEmpty());
    }

    // Status can be changed to ACTIVE.
    @Test
    void statusCanBeChangedToActive() {
        User user = new User("John", "Doe", "john@test.com", "pass",
                "123456789", "0612345678", Role.CUSTOMER);

        user.setStatus(UserStatus.ACTIVE);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    // Status can be changed to CLOSED.
    @Test
    void statusCanBeChangedToClosed() {
        User user = new User("John", "Doe", "john@test.com", "pass",
                "123456789", "0612345678", Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        user.setStatus(UserStatus.CLOSED);

        assertEquals(UserStatus.CLOSED, user.getStatus());
    }

    // Employee role is stored correctly.
    @Test
    void employeeRoleIsStored() {
        User user = new User("Admin", "User", "admin@bank.com", "pass",
                "111111111", "0600000000", Role.EMPLOYEE);

        assertEquals(Role.EMPLOYEE, user.getRole());
    }
}
