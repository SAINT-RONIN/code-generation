package com.banking.policy;

import com.banking.exception.DailyLimitExceededException;
import com.banking.exception.InsufficientFundsException;
import com.banking.exception.InvalidTransferException;
import com.banking.exception.UnauthorizedAccountAccessException;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Enforces all business rules for transactions (transfers, deposits, withdrawals). */
@Component
public class TransactionPolicy {

    // Prevents transferring money from an account to itself
    public void requireDifferentAccounts(String fromIban, String toIban) {
        if (fromIban.equals(toIban)) {
            throw new InvalidTransferException("Source and destination account cannot be the same");
        }
    }

    // Verifies the logged-in customer owns the given account
    public void requireCallerOwnsAccount(Account account, Long callerUserId) {
        if (!account.getUser().getId().equals(callerUserId)) {
            throw new UnauthorizedAccountAccessException("You do not have access to account: " + account.getIban());
        }
    }

    // Own-account: any direction allowed. External: only checking → checking
    public void validateCustomerTransfer(Account from, Account to) {
        boolean isOwnAccounts = from.getUser().getId().equals(to.getUser().getId());

        if (from.getAccountType() == AccountType.SAVINGS && !isOwnAccounts) {
            throw new InvalidTransferException("Savings accounts can only transfer to own accounts");
        }

        if (!isOwnAccounts && to.getAccountType() == AccountType.SAVINGS) {
            throw new InvalidTransferException("External transfers must target a checking account");
        }
    }

    // Employees can only transfer between checking accounts of different customers
    public void validateEmployeeTransfer(Account from, Account to) {
        if (from.getAccountType() != AccountType.CHECKING || to.getAccountType() != AccountType.CHECKING) {
            throw new InvalidTransferException("Employee transfers must be between checking accounts");
        }
        if (from.getUser().getId().equals(to.getUser().getId())) {
            throw new InvalidTransferException("Employee transfers must be between different customers");
        }
    }

    // Balance after debit must stay above the absolute limit
    public void enforceAbsoluteLimit(Account account, BigDecimal amount) {
        if (account.getBalance().subtract(amount).compareTo(account.getAbsoluteLimit()) < 0) {
            throw new InsufficientFundsException(account.getIban());
        }
    }

    // Today's total outgoing (including this transaction) must not exceed the daily limit
    public void enforceDailyLimit(Account account, BigDecimal amount, BigDecimal todaySpent) {
        if (todaySpent.add(amount).compareTo(account.getDailyLimit()) > 0) {
            throw new DailyLimitExceededException(account.getIban());
        }
    }
}
