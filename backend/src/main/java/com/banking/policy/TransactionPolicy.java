package com.banking.policy;

import com.banking.exception.DailyLimitExceededException;
import com.banking.exception.InsufficientFundsException;
import com.banking.exception.InvalidTransferException;
import com.banking.exception.UnauthorizedAccountAccessException;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Enforces all business rules for transactions (transfers, deposits, withdrawals).
 * Separated from TransactionService to keep validation logic clean and testable.
 *
 * Rules enforced:
 * - Source and destination must be different accounts
 * - The caller must own the source account
 * - Savings accounts can only transfer to the same customer's accounts
 * - External transfers must target a checking account
 * - Employee transfers must be between checking accounts of different customers
 * - Balance after debit must stay above the absolute limit
 * - Total daily outgoing must not exceed the daily limit
 */
@Component // Registered as a Spring bean so it can be injected into TransactionService
public class TransactionPolicy {

    // Prevents transferring money from an account to itself
    public void requireDifferentAccounts(String fromIban, String toIban) {
        if (fromIban.equals(toIban)) {
            throw new InvalidTransferException("Source and destination account cannot be the same");
        }
    }

    // Verifies that the logged-in customer owns the given account (matched by user ID)
    public void requireCallerOwnsAccount(Account account, Long callerUserId) {
        if (!account.getUser().getId().equals(callerUserId)) {
            throw new UnauthorizedAccountAccessException("You do not have access to account: " + account.getIban());
        }
    }

    /**
     * Validates customer transfer rules based on account ownership and types.
     *
     * Own-account transfers (same customer): allowed in any direction (checking ↔ savings).
     * External transfers (different customers):
     *   - Cannot send FROM a savings account (savings → external is blocked)
     *   - Cannot send TO a savings account (external → savings is blocked)
     *   - Only checking → checking is allowed for external transfers
     */
    public void validateCustomerTransfer(Account from, Account to) {
        // Check if both accounts belong to the same customer
        boolean isOwnAccounts = from.getUser().getId().equals(to.getUser().getId());

        // Savings accounts can only transfer to the customer's own accounts
        if (from.getAccountType() == AccountType.SAVINGS && !isOwnAccounts) {
            throw new InvalidTransferException("Savings accounts can only transfer to own accounts");
        }

        // External transfers cannot target a savings account (must go to checking)
        if (!isOwnAccounts && to.getAccountType() == AccountType.SAVINGS) {
            throw new InvalidTransferException("External transfers must target a checking account");
        }
    }

    /**
     * Validates employee transfer rules.
     * Employees can only transfer between checking accounts of different customers.
     * This prevents employees from moving money within a single customer's accounts
     * or involving savings accounts.
     */
    public void validateEmployeeTransfer(Account from, Account to) {
        // Both accounts must be CHECKING type
        if (from.getAccountType() != AccountType.CHECKING || to.getAccountType() != AccountType.CHECKING) {
            throw new InvalidTransferException("Employee transfers must be between checking accounts");
        }
        // Accounts must belong to different customers
        if (from.getUser().getId().equals(to.getUser().getId())) {
            throw new InvalidTransferException("Employee transfers must be between different customers");
        }
    }

    /**
     * Checks that the account balance after the debit won't drop below the absolute limit.
     * The absolute limit is the minimum balance the account must maintain (can be negative for overdraft).
     * Example: balance = 500, absolute limit = -100, amount = 700 → allowed (500 - 700 = -200 < -100 → blocked)
     */
    public void enforceAbsoluteLimit(Account account, BigDecimal amount) {
        // balance - amount must be >= absoluteLimit
        if (account.getBalance().subtract(amount).compareTo(account.getAbsoluteLimit()) < 0) {
            throw new InsufficientFundsException(account.getIban());
        }
    }

    /**
     * Checks that today's total outgoing amount (including this new transaction)
     * does not exceed the account's daily spending limit.
     * todaySpent is the sum of all outgoing transactions already made today.
     */
    public void enforceDailyLimit(Account account, BigDecimal amount, BigDecimal todaySpent) {
        // todaySpent + amount must be <= dailyLimit
        if (todaySpent.add(amount).compareTo(account.getDailyLimit()) > 0) {
            throw new DailyLimitExceededException(account.getIban());
        }
    }
}
