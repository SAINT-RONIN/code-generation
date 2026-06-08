package com.banking.service;

import com.banking.dto.TransactionFilter;
import com.banking.dto.TransactionRequest;
import com.banking.dto.TransactionResponse;
import com.banking.exception.InvalidTransferException;
import com.banking.exception.UnauthorizedAccountAccessException;
import com.banking.mapper.TransactionMapper;
import com.banking.model.Account;
import com.banking.model.Transaction.TransactionType;
import com.banking.policy.TransactionPolicy;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.service.interfaces.ITransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Core transaction logic: transfers, deposits, and withdrawals.
 * Enforces business rules through TransactionPolicy (ownership, limits, account type rules).
 *
 * The entire class is @Transactional so that every operation (balance changes + transaction record)
 * happens atomically — if anything fails, all changes are rolled back.
 */
@Service
@Transactional // All methods run inside a database transaction by default
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionPolicy transactionPolicy;

    // Constructor injection of all dependencies
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                              TransactionMapper transactionMapper, TransactionPolicy transactionPolicy) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = transactionMapper;
        this.transactionPolicy = transactionPolicy;
    }

    /**
     * Entry point for customer transactions. Automatically determines the transaction type
     * based on which IBANs are provided and routes to the correct handler.
     */
    @Override
    public TransactionResponse createCustomerTransaction(TransactionRequest request, Long callerUserId, String performedBy) {
        return switch (inferType(request)) {
            case TRANSFER -> customerTransfer(request, callerUserId, performedBy);
            case DEPOSIT -> deposit(request, callerUserId, performedBy);
            case WITHDRAWAL -> withdrawal(request, callerUserId, performedBy);
        };
    }

    /**
     * Entry point for employee transactions. Employees can only create transfers
     * (not deposits or withdrawals) — those are customer-only ATM operations.
     */
    @Override
    public TransactionResponse createEmployeeTransfer(TransactionRequest request, String performedBy) {
        if (inferType(request) != TransactionType.TRANSFER) {
            throw new UnauthorizedAccountAccessException("Deposits and withdrawals are only available to customers");
        }
        return employeeTransfer(request, performedBy);
    }

    /**
     * Infers the transaction type from which IBAN fields are provided:
     * - Both fromIban and toIban → TRANSFER (moving money between accounts)
     * - Only toIban → DEPOSIT (adding cash to an account, e.g. ATM)
     * - Only fromIban → WITHDRAWAL (taking cash out, e.g. ATM)
     * - Neither → invalid request
     */
    private TransactionType inferType(TransactionRequest request) {
        boolean hasFrom = request.fromIban() != null && !request.fromIban().isBlank();
        boolean hasTo = request.toIban() != null && !request.toIban().isBlank();
        if (hasFrom && hasTo) return TransactionType.TRANSFER;
        if (hasTo) return TransactionType.DEPOSIT;
        if (hasFrom) return TransactionType.WITHDRAWAL;
        throw new InvalidTransferException("Either fromIban or toIban must be provided");
    }

    /**
     * Customer transfer: moves money between two accounts.
     * Supports both own-account transfers (checking ↔ savings) and external transfers.
     * The caller must own the source (from) account.
     *
     * Own-account transfers allow checking ↔ savings in any direction.
     * External transfers require the destination to be a checking account.
     */
    private TransactionResponse customerTransfer(TransactionRequest request, Long callerUserId, String performedBy) {
        // Prevent transferring to the same account
        transactionPolicy.requireDifferentAccounts(request.fromIban(), request.toIban());
        Account from = accountRepository.findRequiredActiveById(request.fromIban());
        Account to = accountRepository.findRequiredActiveById(request.toIban());
        // Verify the caller owns the source account
        transactionPolicy.requireCallerOwnsAccount(from, callerUserId);
        // Apply transfer rules (savings restrictions, own-account vs external)
        transactionPolicy.validateCustomerTransfer(from, to);
        // Check funds and limits, then debit the source account
        deductWithLimitChecks(from, request.amount());
        // Credit the destination account
        to.credit(request.amount());
        return transactionMapper.toResponse(transactionRepository.record(
                request.fromIban(), request.toIban(), request.amount(),
                performedBy, request.description(), TransactionType.TRANSFER
        ));
    }

    /**
     * Employee transfer: moves money between two checking accounts of different customers.
     * Employees cannot transfer to/from savings or between accounts of the same customer.
     */
    private TransactionResponse employeeTransfer(TransactionRequest request, String performedBy) {
        transactionPolicy.requireDifferentAccounts(request.fromIban(), request.toIban());
        Account from = accountRepository.findRequiredActiveById(request.fromIban());
        Account to = accountRepository.findRequiredActiveById(request.toIban());
        // Enforce employee-specific rules: checking-to-checking, different customers
        transactionPolicy.validateEmployeeTransfer(from, to);
        deductWithLimitChecks(from, request.amount());
        to.credit(request.amount());
        return transactionMapper.toResponse(transactionRepository.record(
                request.fromIban(), request.toIban(), request.amount(),
                performedBy, request.description(), TransactionType.TRANSFER
        ));
    }

    /**
     * Deposit: adds money to a customer's own account (e.g. cash deposit at ATM).
     * Only the account owner can deposit into their account.
     */
    private TransactionResponse deposit(TransactionRequest request, Long callerUserId, String performedBy) {
        Account account = accountRepository.findRequiredActiveById(request.toIban());
        transactionPolicy.requireCallerOwnsAccount(account, callerUserId);
        account.credit(request.amount());
        // fromIban is null for deposits (money comes from outside the system)
        return transactionMapper.toResponse(transactionRepository.record(
                null, request.toIban(), request.amount(),
                performedBy, request.description(), TransactionType.DEPOSIT
        ));
    }

    /**
     * Withdrawal: takes money out of a customer's own account (e.g. ATM withdrawal).
     * Subject to absolute limit and daily limit checks.
     */
    private TransactionResponse withdrawal(TransactionRequest request, Long callerUserId, String performedBy) {
        Account account = accountRepository.findRequiredActiveById(request.fromIban());
        transactionPolicy.requireCallerOwnsAccount(account, callerUserId);
        deductWithLimitChecks(account, request.amount());
        // toIban is null for withdrawals (money leaves the system)
        return transactionMapper.toResponse(transactionRepository.record(
                request.fromIban(), null, request.amount(),
                performedBy, request.description(), TransactionType.WITHDRAWAL
        ));
    }

    /**
     * Returns paginated transaction history for a customer.
     * Only shows transactions involving the customer's own accounts (filtered by their IBANs).
     */
    @Override
    @Transactional(readOnly = true) // Read-only optimization — no write locks needed
    public Page<TransactionResponse> findCustomerTransactions(TransactionFilter filter, Pageable pageable, Long callerUserId) {
        // Get all IBANs owned by this customer, then filter transactions to only those IBANs
        return transactionRepository.findByFilterForUser(filter, pageable, accountRepository.findOwnedIbansByUserId(callerUserId))
                .map(transactionMapper::toResponse);
    }

    /**
     * Returns paginated transaction history for employees (no account ownership filter).
     * Employees can see all transactions across the entire banking system.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAllTransactions(TransactionFilter filter, Pageable pageable) {
        return transactionRepository.findByFilter(filter, pageable).map(transactionMapper::toResponse);
    }

    /**
     * Validates that the account has sufficient funds and hasn't exceeded the daily limit,
     * then debits the amount. This method combines three checks in order:
     * 1. Absolute limit: balance after debit must stay above the account's minimum (absolute limit)
     * 2. Daily limit: total outgoing today + this amount must not exceed the daily limit
     * 3. Debit: subtract the amount from the account balance
     */
    private void deductWithLimitChecks(Account account, BigDecimal amount) {
        // Check that balance minus amount stays above the absolute limit (minimum balance)
        transactionPolicy.enforceAbsoluteLimit(account, amount);
        // Sum all outgoing transactions for this account today to check the daily limit
        BigDecimal todaySpent = transactionRepository.sumOutgoingTodayForIban(account.getIban());
        transactionPolicy.enforceDailyLimit(account, amount, todaySpent);
        // All checks passed — subtract the amount from the account balance
        account.debit(amount);
    }
}
