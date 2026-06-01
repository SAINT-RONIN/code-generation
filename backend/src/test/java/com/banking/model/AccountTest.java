package com.banking.model;

import com.banking.model.Account.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTest {

    @Test
    void creditIncreasesBalance() {
        Account account = buildAccount("500.00");
        account.credit(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("600.00"), account.getBalance());
    }

    @Test
    void debitDecreasesBalance() {
        Account account = buildAccount("500.00");
        account.debit(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("400.00"), account.getBalance());
    }

    @Test
    void newAccountStartsActive() {
        Account account = buildAccount("0");
        assertTrue(account.isActive());
    }

    @Test
    void deactivateSetsActiveFalse() {
        Account account = buildAccount("0");
        account.deactivate();
        assertFalse(account.isActive());
    }

    @Test
    void activateRestoresActiveAfterDeactivate() {
        Account account = buildAccount("0");
        account.deactivate();
        account.activate();
        assertTrue(account.isActive());
    }

    @Test
    void updateDailyLimitChangesLimit() {
        Account account = buildAccount("0");
        account.updateDailyLimit(new BigDecimal("5000.00"));
        assertEquals(new BigDecimal("5000.00"), account.getDailyLimit());
    }

    @Test
    void updateAbsoluteLimitChangesLimit() {
        Account account = buildAccount("0");
        account.updateAbsoluteLimit(new BigDecimal("-500.00"));
        assertEquals(new BigDecimal("-500.00"), account.getAbsoluteLimit());
    }

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
