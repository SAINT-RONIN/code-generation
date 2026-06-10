package com.banking.model;

import com.banking.model.Account.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTest {

    // Credit adds the amount to the balance.
    @Test
    void creditIncreasesBalance() {
        Account account = buildAccount("500.00");
        account.credit(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("600.00"), account.getBalance());
    }

    // Debit subtracts the amount from the balance.
    @Test
    void debitDecreasesBalance() {
        Account account = buildAccount("500.00");
        account.debit(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("400.00"), account.getBalance());
    }

    // New accounts are active by default.
    @Test
    void newAccountStartsActive() {
        Account account = buildAccount("0");
        assertTrue(account.isActive());
    }

    // Deactivating sets the account to inactive.
    @Test
    void deactivateSetsActiveFalse() {
        Account account = buildAccount("0");
        account.deactivate();
        assertFalse(account.isActive());
    }

    // Activating restores the account after deactivation.
    @Test
    void activateRestoresActiveAfterDeactivate() {
        Account account = buildAccount("0");
        account.deactivate();
        account.activate();
        assertTrue(account.isActive());
    }

    // Daily limit can be updated.
    @Test
    void updateDailyLimitChangesLimit() {
        Account account = buildAccount("0");
        account.updateDailyLimit(new BigDecimal("5000.00"));
        assertEquals(new BigDecimal("5000.00"), account.getDailyLimit());
    }

    // Absolute limit can be updated.
    @Test
    void updateAbsoluteLimitChangesLimit() {
        Account account = buildAccount("0");
        account.updateAbsoluteLimit(new BigDecimal("-500.00"));
        assertEquals(new BigDecimal("-500.00"), account.getAbsoluteLimit());
    }

    // Constructor without balance defaults to zero.
    @Test
    void constructorWithoutBalanceStartsAtZero() {
        Account account = new Account("NL01", AccountType.CHECKING, BigDecimal.ZERO, new BigDecimal("2000"), buildOwner());
        assertEquals(BigDecimal.ZERO, account.getBalance());
    }

    private User buildOwner() {
        return new User("Test", "User", "t@t.com", "pass", "123456789", "0600000000", User.Role.CUSTOMER);
    }

    private Account buildAccount(String balance) {
        return new Account("NL01BANK0000000001", AccountType.CHECKING,
                new BigDecimal(balance), BigDecimal.ZERO, new BigDecimal("2000"), buildOwner());
    }
}
