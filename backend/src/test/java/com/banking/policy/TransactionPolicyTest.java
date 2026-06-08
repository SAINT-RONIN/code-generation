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

/**
 * Unit tests for TransactionPolicy.
 * Verifies all business rules for transactions:
 * - Same-account transfers are blocked
 * - Account ownership is enforced
 * - Customer transfer rules (savings restrictions, own-account vs external)
 * - Employee transfer rules (checking-to-checking, different customers)
 * - Absolute limit (minimum balance) enforcement
 * - Daily limit enforcement
 *
 * Pure unit tests — no Spring context, no mocks needed (policy is a plain POJO).
 */
class TransactionPolicyTest {

    private TransactionPolicy policy;

    @BeforeEach
    void setUp() {
        // TransactionPolicy has no dependencies — just instantiate it directly
        policy = new TransactionPolicy();
    }

    // ── Require different accounts ────────────────────

    /** Transferring to the same account is meaningless and should be blocked */
    @Test
    void requireDifferentAccountsThrowsWhenSameIban() {
        assertThrows(InvalidTransferException.class,
                () -> policy.requireDifferentAccounts("NL01BANK0000000001", "NL01BANK0000000001"));
    }

    /** Different IBANs are a valid transfer pair */
    @Test
    void requireDifferentAccountsPassesWhenDifferentIbans() {
        assertDoesNotThrow(
                () -> policy.requireDifferentAccounts("NL01BANK0000000001", "NL01BANK0000000002"));
    }

    // ── Require caller owns account ────────────────────

    /** Customers should be able to operate on their own accounts (matching user ID) */
    @Test
    void requireCallerOwnsAccountPassesWhenCallerIsOwner() {
        Account account = buildCheckingAccount(buildUser(1L));
        assertDoesNotThrow(() -> policy.requireCallerOwnsAccount(account, 1L));
    }

    /** Customers must not be able to operate on someone else's account (different user ID) */
    @Test
    void requireCallerOwnsAccountThrowsWhenCallerIsNotOwner() {
        Account account = buildCheckingAccount(buildUser(1L));
        assertThrows(UnauthorizedAccountAccessException.class,
                () -> policy.requireCallerOwnsAccount(account, 99L));
    }

    // ── Customer transfer rules ────────────────────

    /** Customers can freely transfer between their own accounts (checking ↔ savings) */
    @Test
    void customerTransferPassesBetweenOwnAccounts() {
        User owner = buildUser(1L);
        Account from = buildSavingsAccount(owner);
        Account to = buildCheckingAccount(owner);
        // Same owner → own-account transfer → allowed in any direction
        assertDoesNotThrow(() -> policy.validateCustomerTransfer(from, to));
    }

    /** Customers can send money from their checking to another customer's checking */
    @Test
    void customerTransferPassesCheckingToCheckingDifferentOwners() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertDoesNotThrow(() -> policy.validateCustomerTransfer(from, to));
    }

    /** Savings accounts cannot send to external customers — savings is for personal use only */
    @Test
    void customerTransferThrowsWhenSavingsToExternalAccount() {
        Account from = buildSavingsAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        // Different owners + savings source → blocked
        assertThrows(InvalidTransferException.class,
                () -> policy.validateCustomerTransfer(from, to));
    }

    /** External transfers must target a checking account, not savings */
    @Test
    void customerTransferThrowsWhenExternalToSavings() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildSavingsAccount(buildUser(2L));
        // Different owners + savings destination → blocked
        assertThrows(InvalidTransferException.class,
                () -> policy.validateCustomerTransfer(from, to));
    }

    // ── Employee transfer rules ────────────────────

    /** Employees can transfer between checking accounts of different customers */
    @Test
    void employeeTransferPassesBetweenCheckingDifferentOwners() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertDoesNotThrow(() -> policy.validateEmployeeTransfer(from, to));
    }

    /** Employees must not touch savings accounts — only checking-to-checking is allowed */
    @Test
    void employeeTransferThrowsWhenFromAccountIsSavings() {
        Account from = buildSavingsAccount(buildUser(1L));
        Account to = buildCheckingAccount(buildUser(2L));
        assertThrows(InvalidTransferException.class,
                () -> policy.validateEmployeeTransfer(from, to));
    }

    /** Same rule: destination must also be a checking account */
    @Test
    void employeeTransferThrowsWhenToAccountIsSavings() {
        Account from = buildCheckingAccount(buildUser(1L));
        Account to = buildSavingsAccount(buildUser(2L));
        assertThrows(InvalidTransferException.class,
                () -> policy.validateEmployeeTransfer(from, to));
    }

    /** Employee transfers must be between different customers — not within the same customer */
    @Test
    void employeeTransferThrowsWhenSameOwner() {
        User owner = buildUser(1L);
        Account from = buildCheckingAccount(owner);
        Account to = buildCheckingAccount(owner);
        // Same owner → blocked for employees (they should not move money within one customer)
        assertThrows(InvalidTransferException.class,
                () -> policy.validateEmployeeTransfer(from, to));
    }

    // ── Absolute limit ────────────────────

    /** If the balance after deduction stays above the absolute limit, the transfer is allowed */
    @Test
    void enforceAbsoluteLimitPassesWhenBalanceRemainsAboveLimit() {
        // Balance: 1000, absolute limit: 0, deducting 500 → remaining 500 ≥ 0 → OK
        Account account = buildAccountWithLimits("1000", "0", "2000");
        assertDoesNotThrow(() -> policy.enforceAbsoluteLimit(account, new BigDecimal("500")));
    }

    /** If the balance after deduction would drop below the absolute limit, the transfer must be blocked */
    @Test
    void enforceAbsoluteLimitThrowsWhenBalanceDropsBelowLimit() {
        // Balance: 100, absolute limit: 0, deducting 200 → remaining -100 < 0 → BLOCKED
        Account account = buildAccountWithLimits("100", "0", "2000");
        assertThrows(InsufficientFundsException.class,
                () -> policy.enforceAbsoluteLimit(account, new BigDecimal("200")));
    }

    /** A negative absolute limit allows overdraft — deduction should pass as long as the floor isn't breached */
    @Test
    void enforceAbsoluteLimitPassesWithOverdraftAllowed() {
        // Balance: 100, absolute limit: -500, deducting 400 → remaining -300 ≥ -500 → OK (overdraft allowed)
        Account account = buildAccountWithLimits("100", "-500", "2000");
        assertDoesNotThrow(() -> policy.enforceAbsoluteLimit(account, new BigDecimal("400")));
    }

    // ── Daily limit ────────────────────

    /** If today's spending plus the new amount stays within the daily limit, the transfer is allowed */
    @Test
    void enforceDailyLimitPassesWhenWithinLimit() {
        // Daily limit: 2000, already spent today: 1000, new amount: 500 → total 1500 ≤ 2000 → OK
        Account account = buildAccountWithLimits("5000", "0", "2000");
        BigDecimal amount = new BigDecimal("500");
        BigDecimal todaySpent = new BigDecimal("1000");
        assertDoesNotThrow(() -> policy.enforceDailyLimit(account, amount, todaySpent));
    }

    /** If today's spending plus the new amount exceeds the daily limit, the transfer must be blocked */
    @Test
    void enforceDailyLimitThrowsWhenExceedsLimit() {
        // Daily limit: 2000, already spent today: 1800, new amount: 500 → total 2300 > 2000 → BLOCKED
        Account account = buildAccountWithLimits("5000", "0", "2000");
        BigDecimal amount = new BigDecimal("500");
        BigDecimal todaySpent = new BigDecimal("1800");
        assertThrows(DailyLimitExceededException.class,
                () -> policy.enforceDailyLimit(account, amount, todaySpent));
    }

    // ── Helpers ────────────────────

    // Creates a User entity with a manually set ID (needed for ownership comparisons)
    private User buildUser(Long id) {
        User user = new User("Test", "User", "test@test.com", "pass", "123456789", "0600000000", User.Role.CUSTOMER);
        user.setId(id);
        return user;
    }

    // Creates a CHECKING account with €1000 balance for the given owner
    private Account buildCheckingAccount(User owner) {
        return new Account("NL01TEST", AccountType.CHECKING,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("2000"), owner);
    }

    // Creates a SAVINGS account with €1000 balance for the given owner
    private Account buildSavingsAccount(User owner) {
        return new Account("NL02TEST", AccountType.SAVINGS,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("2000"), owner);
    }

    // Creates an account with specific balance and limits for limit enforcement tests
    private Account buildAccountWithLimits(String balance, String absoluteLimit, String dailyLimit) {
        return new Account("NL01TEST", AccountType.CHECKING, new BigDecimal(balance),
                new BigDecimal(absoluteLimit), new BigDecimal(dailyLimit), buildUser(1L));
    }
}
