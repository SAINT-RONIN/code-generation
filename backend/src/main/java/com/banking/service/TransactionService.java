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

@Service
@Transactional // All methods run inside a database transaction by default
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionPolicy transactionPolicy;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
            TransactionMapper transactionMapper, TransactionPolicy transactionPolicy) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = transactionMapper;
        this.transactionPolicy = transactionPolicy;
    }

    @Override
    public TransactionResponse createCustomerTransaction(TransactionRequest request, Long callerUserId,
            String performedBy) {
        return switch (inferType(request)) {
            case TRANSFER -> customerTransfer(request, callerUserId, performedBy);
            case DEPOSIT -> deposit(request, callerUserId, performedBy);
            case WITHDRAWAL -> withdrawal(request, callerUserId, performedBy);
        };
    }

    @Override
    public TransactionResponse createEmployeeTransfer(TransactionRequest request, String performedBy) {
        if (inferType(request) != TransactionType.TRANSFER) {
            throw new UnauthorizedAccountAccessException("Deposits and withdrawals are only available to customers");
        }
        return employeeTransfer(request, performedBy);
    }

    private TransactionType inferType(TransactionRequest request) {
        boolean hasFrom = request.fromIban() != null && !request.fromIban().isBlank();
        boolean hasTo = request.toIban() != null && !request.toIban().isBlank();
        if (hasFrom && hasTo)
            return TransactionType.TRANSFER;
        if (hasTo)
            return TransactionType.DEPOSIT;
        if (hasFrom)
            return TransactionType.WITHDRAWAL;
        throw new InvalidTransferException("Either fromIban or toIban must be provided");
    }

    private TransactionResponse customerTransfer(TransactionRequest request, Long callerUserId, String performedBy) {
        transactionPolicy.requireDifferentAccounts(request.fromIban(), request.toIban());
        Account from = accountRepository.findRequiredActiveById(request.fromIban());
        Account to = accountRepository.findRequiredActiveById(request.toIban());
        transactionPolicy.requireCallerOwnsAccount(from, callerUserId);
        transactionPolicy.validateCustomerTransfer(from, to);
        deductWithLimitChecks(from, request.amount());
        to.credit(request.amount());
        return transactionMapper.toResponse(transactionRepository.record(
                request.fromIban(), request.toIban(), request.amount(),
                performedBy, request.description(), TransactionType.TRANSFER));
    }

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
                performedBy, request.description(), TransactionType.TRANSFER));
    }

    private TransactionResponse deposit(TransactionRequest request, Long callerUserId, String performedBy) {
        Account account = accountRepository.findRequiredActiveById(request.toIban());
        transactionPolicy.requireCallerOwnsAccount(account, callerUserId);
        account.credit(request.amount());
        // fromIban is null for deposits (money comes from outside the system)
        return transactionMapper.toResponse(transactionRepository.record(
                null, request.toIban(), request.amount(),
                performedBy, request.description(), TransactionType.DEPOSIT));
    }

    private TransactionResponse withdrawal(TransactionRequest request, Long callerUserId, String performedBy) {
        Account account = accountRepository.findRequiredActiveById(request.fromIban());
        transactionPolicy.requireCallerOwnsAccount(account, callerUserId);
        deductWithLimitChecks(account, request.amount());
        // toIban is null for withdrawals (money leaves the system)
        return transactionMapper.toResponse(transactionRepository.record(
                request.fromIban(), null, request.amount(),
                performedBy, request.description(), TransactionType.WITHDRAWAL));
    }

    @Override
    @Transactional(readOnly = true) // Read-only optimization — no write locks needed
    public Page<TransactionResponse> findCustomerTransactions(TransactionFilter filter, Pageable pageable,
            Long callerUserId) {
        // Get all IBANs owned by this customer, then filter transactions to only those
        // IBANs
        return transactionRepository
                .findByFilterForUser(filter, pageable, accountRepository.findOwnedIbansByUserId(callerUserId))
                .map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAllTransactions(TransactionFilter filter, Pageable pageable) {
        return transactionRepository.findByFilter(filter, pageable).map(transactionMapper::toResponse);
    }

    private void deductWithLimitChecks(Account account, BigDecimal amount) {
        transactionPolicy.enforceAbsoluteLimit(account, amount);
        BigDecimal todaySpent = transactionRepository.sumOutgoingTodayForIban(account.getIban());
        transactionPolicy.enforceDailyLimit(account, amount, todaySpent);
        account.debit(amount);
    }
}
