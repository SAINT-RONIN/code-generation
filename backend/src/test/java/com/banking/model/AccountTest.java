package com.banking.model;

import com.banking.model.Account.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Account entity model.
 * Verifies core domain behaviour: credit/debit operations, active/inactive state,
 * limit updates, and default values on construction.
 *
 * These are pure unit tests — no Spring context, no database.
 */
class AccountTest {

    /** credit() should add the amount to the current balance */
    @Test
    void creditIncreasesBalance() {
        Account account = buildAccount("500.00");
        account.credit(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("600.00"), account.getBalance());
    }

    /** debit() should subtract the amount from the current balance */
    @Test
    void debitDecreasesBalance() {
        Account account = buildAccount("500.00");
        account.debit(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("400.00"), account.getBalance());
    }

    /** Accounts should be active by default so they are usable immediately after creation */
    @Test
    void newAccountStartsActive() {
        Account account = buildAccount("0");
        assertTrue(account.isActive());
    }

    /** Deactivating an account should prevent it from being used in transactions */
    @Test
    void deactivateSetsActiveFalse() {
        Account account = buildAccount("0");
        account.deactivate();
        assertFalse(account.isActive());
    }

    /** Reactivating should restore the account to an active state after deactivation */
    @Test
    void activateRestoresActiveAfterDeactivate() {
        Account account = buildAccount("0");
        account.deactivate();
        account.activate();
        assertTrue(account.isActive());
    }

    /** Employees should be able to update the daily transfer limit */
    @Test
    void updateDailyLimitChangesLimit() {
        Account account = buildAccount("0");
        account.updateDailyLimit(new BigDecimal("5000.00"));
        assertEquals(new BigDecimal("5000.00"), account.getDailyLimit());
    }

    /** Employees should be able to update the overdraft floor (absolute limit) */
    @Test
    void updateAbsoluteLimitChangesLimit() {
        Account account = buildAccount("0");
        account.updateAbsoluteLimit(new BigDecimal("-500.00"));
        assertEquals(new BigDecimal("-500.00"), account.getAbsoluteLimit());
    }

    /** The convenience constructor (without balance) should default to zero balance */
    @Test
    void constructorWithoutBalanceStartsAtZero() {
        Account account = new Account("NL01", AccountType.CHECKING, BigDecimal.ZERO, new BigDecimal("2000"), buildOwner());
        assertEquals(BigDecimal.ZERO, account.getBalance());
    }

    // ── Helpers ────────────────────

    // Creates a dummy owner for account construction
    private User buildOwner() {
        return new User("Test", "User", "t@t.com", "pass", "123456789", "0600000000", User.Role.CUSTOMER);
    }

    // Creates a checking account with the given balance and standard limits
    private Account buildAccount(String balance) {
        return new Account("NL01BANK0000000001", AccountType.CHECKING,
                new BigDecimal(balance), BigDecimal.ZERO, new BigDecimal("2000"), buildOwner());
    }
}
