package com.banking.policy;

import com.banking.exception.DailyLimitExceededException;
import com.banking.exception.InsufficientFundsException;
import com.banking.exception.InvalidTransferException;
import com.banking.exception.UnauthorizedAccountAccessException;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionPolicyTest {

    private TransactionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new TransactionPolicy();
    }

    // ── Require different accounts ────────────────────

    // Same IBAN on both sides is rejected.
    @Test
    void requireDifferentAccountsThrowsWhenSameIban() {
        assertThrows(InvalidTransferException.class,
                () -> policy.requireDifferentAccounts("NL01BANK0000000001", "NL01BANK0000000001"));
    }

    // Different IBANs pass the check.
    @Test
    void requireDifferentAccountsPassesWhenDifferentIbans() {
        assertDoesNotThrow(
                () -> policy.requireDifferentAccounts("NL01BANK0000000001", "NL01BANK0000000002"));
    }

    // ── Require caller owns account ────────────────────

    // Owner can operate on their own account.
    @Test
    void requireCallerOwnsAccountPassesWhenCallerIsOwner() {
        Account account = buildCheckingAccount(buildUser(1L));
        assertDoesNotThrow(() -> policy.requireCallerOwnsAccount(account, 1L));
    }

    // Non-owner is rejected from operating on someone else's account.
    @Test
    void requireCallerOwnsAccountThrowsWhenCallerIsNotOwner() {
        Account account = buildCheckingAccount(buildUser(1L));
        assertThrows(UnauthorizedAccountAccessException.class,
                () -> policy.requireCallerOwnsAccount(account, 99L));
    }

    // ── Customer transfer rules ────────────────────

    // Customer can transfer between their own accounts.
    @Test
    void customerTransferPassesBetweenOwnAccounts() {
        User owner = buildUser(1L);
        Account from = buildSavingsAccount(owner);
        Account to = buildCheckingAccount(owner);
        assertDoesNotThrow(() -> policy.validateCustomerTransfer(from, to));
    }

    // Customer can send from checking to another customer's checking.
    @Test
    void customerTransferPassesCheckingToCheckingDifferentOwners() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertDoesNotThrow(() -> policy.validateCustomerTransfer(from, to));
    }

    // Savings to external customer is rejected.
    @Test
    void customerTransferThrowsWhenSavingsToExternalAccount() {
        Account from = buildSavingsAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertThrows(InvalidTransferException.class,
                () -> policy.validateCustomerTransfer(from, to));
    }

    // External transfer to savings is rejected.
    @Test
    void customerTransferThrowsWhenExternalToSavings() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildSavingsAccount(buildUser(2L));
        assertThrows(InvalidTransferException.class,
                () -> policy.validateCustomerTransfer(from, to));
    }

    // ── Employee transfer rules ────────────────────

    // Employee can transfer between checking accounts of different customers.
    @Test
    void employeeTransferPassesBetweenCheckingDifferentOwners() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertDoesNotThrow(() -> policy.validateEmployeeTransfer(from, to));
    }

    // Employee transfer from savings is rejected.
    @Test
    void employeeTransferThrowsWhenFromAccountIsSavings() {
        Account from = buildSavingsAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertThrows(InvalidTransferException.class,
                () -> policy.validateEmployeeTransfer(from, to));
    }

    // Employee transfer to savings is rejected.
    @Test
    void employeeTransferThrowsWhenToAccountIsSavings() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildSavingsAccount(buildUser(2L));
        assertThrows(InvalidTransferException.class,
                () -> policy.validateEmployeeTransfer(from, to));
    }

    // Employee transfer within the same customer is rejected.
    @Test
    void employeeTransferThrowsWhenSameOwner() {
        User owner = buildUser(1L);
        Account from = buildCheckingAccount(owner);
        Account to = buildCheckingAccount(owner);
        assertThrows(InvalidTransferException.class,
                () -> policy.validateEmployeeTransfer(from, to));
    }

    // ── Absolute limit ────────────────────

    // Balance stays above the absolute limit so the transfer is allowed.
    @Test
    void enforceAbsoluteLimitPassesWhenBalanceRemainsAboveLimit() {
        Account account = buildAccountWithLimits("1000", "0", "2000");
        assertDoesNotThrow(() -> policy.enforceAbsoluteLimit(account, new BigDecimal("500")));
    }

    // Balance would drop below the absolute limit so the transfer is blocked.
    @Test
    void enforceAbsoluteLimitThrowsWhenBalanceDropsBelowLimit() {
        Account account = buildAccountWithLimits("100", "0", "2000");
        assertThrows(InsufficientFundsException.class,
                () -> policy.enforceAbsoluteLimit(account, new BigDecimal("200")));
    }

    // Negative absolute limit allows overdraft within the floor.
    @Test
    void enforceAbsoluteLimitPassesWithOverdraftAllowed() {
        Account account = buildAccountWithLimits("100", "-500", "2000");
        assertDoesNotThrow(() -> policy.enforceAbsoluteLimit(account, new BigDecimal("400")));
    }

    // ── Daily limit ────────────────────

    // Today's spending plus the new amount is within the daily limit.
    @Test
    void enforceDailyLimitPassesWhenWithinLimit() {
        Account account = buildAccountWithLimits("5000", "0", "2000");
        assertDoesNotThrow(() -> policy.enforceDailyLimit(account, new BigDecimal("500"), new BigDecimal("1000")));
    }

    // Today's spending plus the new amount exceeds the daily limit.
    @Test
    void enforceDailyLimitThrowsWhenExceedsLimit() {
        Account account = buildAccountWithLimits("5000", "0", "2000");
        assertThrows(DailyLimitExceededException.class,
                () -> policy.enforceDailyLimit(account, new BigDecimal("500"), new BigDecimal("1800")));
    }

    // ── Helpers ────────────────────

    private User buildUser(Long id) {
        User user = new User("Test", "User", "test@test.com", "pass", "123456789", "0600000000", User.Role.CUSTOMER);
        user.setId(id);
        return user;
    }

    private Account buildCheckingAccount(User owner) {
        return new Account("NL01TEST", AccountType.CHECKING,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("2000"), owner);
    }

    private Account buildSavingsAccount(User owner) {
        return new Account("NL02TEST", AccountType.SAVINGS,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("2000"), owner);
    }

    private Account buildAccountWithLimits(String balance, String absoluteLimit, String dailyLimit) {
        return new Account("NL01TEST", AccountType.CHECKING, new BigDecimal(balance),
                new BigDecimal(absoluteLimit), new BigDecimal(dailyLimit), buildUser(1L));
    }
}
