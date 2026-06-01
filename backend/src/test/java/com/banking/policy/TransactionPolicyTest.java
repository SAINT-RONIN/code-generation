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

    @Test
    void requireDifferentAccountsThrowsWhenSameIban() {
        assertThrows(InvalidTransferException.class,
                () -> policy.requireDifferentAccounts("NL01BANK0000000001", "NL01BANK0000000001"));
    }

    @Test
    void requireDifferentAccountsPassesWhenDifferentIbans() {
        assertDoesNotThrow(
                () -> policy.requireDifferentAccounts("NL01BANK0000000001", "NL01BANK0000000002"));
    }

    // ── Require caller owns account ────────────────────

    @Test
    void requireCallerOwnsAccountPassesWhenCallerIsOwner() {
        Account account = buildCheckingAccount(buildUser(1L));
        assertDoesNotThrow(() -> policy.requireCallerOwnsAccount(account, 1L));
    }

    @Test
    void requireCallerOwnsAccountThrowsWhenCallerIsNotOwner() {
        Account account = buildCheckingAccount(buildUser(1L));
        assertThrows(UnauthorizedAccountAccessException.class,
                () -> policy.requireCallerOwnsAccount(account, 99L));
    }

    // ── Customer transfer rules ────────────────────

    @Test
    void customerTransferPassesBetweenOwnAccounts() {
        User owner = buildUser(1L);
        Account from = buildSavingsAccount(owner);
        Account to = buildCheckingAccount(owner);
        assertDoesNotThrow(() -> policy.validateCustomerTransfer(from, to));
    }

    @Test
    void customerTransferPassesCheckingToCheckingDifferentOwners() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertDoesNotThrow(() -> policy.validateCustomerTransfer(from, to));
    }

    @Test
    void customerTransferThrowsWhenSavingsToExternalAccount() {
        Account from = buildSavingsAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertThrows(InvalidTransferException.class,
                () -> policy.validateCustomerTransfer(from, to));
    }

    @Test
    void customerTransferThrowsWhenExternalToSavings() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildSavingsAccount(buildUser(2L));
        assertThrows(InvalidTransferException.class,
                () -> policy.validateCustomerTransfer(from, to));
    }

    // ── Employee transfer rules ────────────────────

    @Test
    void employeeTransferPassesBetweenCheckingDifferentOwners() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertDoesNotThrow(() -> policy.validateEmployeeTransfer(from, to));
    }

    @Test
    void employeeTransferThrowsWhenFromAccountIsSavings() {
        Account from = buildSavingsAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertThrows(InvalidTransferException.class,
                () -> policy.validateEmployeeTransfer(from, to));
    }

    @Test
    void employeeTransferThrowsWhenToAccountIsSavings() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildSavingsAccount(buildUser(2L));
        assertThrows(InvalidTransferException.class,
                () -> policy.validateEmployeeTransfer(from, to));
    }

    @Test
    void employeeTransferThrowsWhenSameOwner() {
        User owner = buildUser(1L);
        Account from = buildCheckingAccount(owner);
        Account to = buildCheckingAccount(owner);
        assertThrows(InvalidTransferException.class,
                () -> policy.validateEmployeeTransfer(from, to));
    }

    // ── Absolute limit ────────────────────

    @Test
    void enforceAbsoluteLimitPassesWhenBalanceRemainsAboveLimit() {
        Account account = buildAccountWithLimits("1000", "0", "2000");
        assertDoesNotThrow(() -> policy.enforceAbsoluteLimit(account, new BigDecimal("500")));
    }

    @Test
    void enforceAbsoluteLimitThrowsWhenBalanceDropsBelowLimit() {
        Account account = buildAccountWithLimits("100", "0", "2000");
        assertThrows(InsufficientFundsException.class,
                () -> policy.enforceAbsoluteLimit(account, new BigDecimal("200")));
    }

    @Test
    void enforceAbsoluteLimitPassesWithOverdraftAllowed() {
        Account account = buildAccountWithLimits("100", "-500", "2000");
        assertDoesNotThrow(() -> policy.enforceAbsoluteLimit(account, new BigDecimal("400")));
    }

    // ── Daily limit ────────────────────

    @Test
    void enforceDailyLimitPassesWhenWithinLimit() {
        Account account = buildAccountWithLimits("5000", "0", "2000");
        BigDecimal amount = new BigDecimal("500");
        BigDecimal todaySpent = new BigDecimal("1000");
        assertDoesNotThrow(() -> policy.enforceDailyLimit(account, amount, todaySpent));
    }

    @Test
    void enforceDailyLimitThrowsWhenExceedsLimit() {
        Account account = buildAccountWithLimits("5000", "0", "2000");
        BigDecimal amount = new BigDecimal("500");
        BigDecimal todaySpent = new BigDecimal("1800");
        assertThrows(DailyLimitExceededException.class,
                () -> policy.enforceDailyLimit(account, amount, todaySpent));
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
