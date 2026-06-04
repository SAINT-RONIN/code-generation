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
@Transactional
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
    public TransactionResponse createTransaction(TransactionRequest request, Long callerUserId, String performedBy, boolean isEmployee) {
        return switch (inferType(request)) {
            case TRANSFER -> transfer(request, callerUserId, performedBy, isEmployee);
            case DEPOSIT -> deposit(request, callerUserId, performedBy, isEmployee);
            case WITHDRAWAL -> withdrawal(request, callerUserId, performedBy, isEmployee);
        };
    }

    private TransactionType inferType(TransactionRequest request) {
        boolean hasFrom = request.fromIban() != null && !request.fromIban().isBlank();
        boolean hasTo = request.toIban() != null && !request.toIban().isBlank();
        if (hasFrom && hasTo) return TransactionType.TRANSFER;
        if (hasTo) return TransactionType.DEPOSIT;
        if (hasFrom) return TransactionType.WITHDRAWAL;
        throw new InvalidTransferException("Either fromIban or toIban must be provided");
    }

    private TransactionResponse transfer(TransactionRequest request, Long callerUserId, String performedBy, boolean isEmployee) {
        transactionPolicy.requireDifferentAccounts(request.fromIban(), request.toIban());
        Account from = accountRepository.findRequiredActiveById(request.fromIban());
        Account to   = accountRepository.findRequiredActiveById(request.toIban());
        if (isEmployee) {
            transactionPolicy.validateEmployeeTransfer(from, to);
        } else {
            transactionPolicy.requireCallerOwnsAccount(from, callerUserId);
            transactionPolicy.validateCustomerTransfer(from, to);
        }
        deductWithLimitChecks(from, request.amount());
        to.credit(request.amount());
        return transactionMapper.toResponse(transactionRepository.record(
                request.fromIban(), request.toIban(), request.amount(),
                performedBy, request.description(), TransactionType.TRANSFER
        ));
    }

    private TransactionResponse deposit(TransactionRequest request, Long callerUserId, String performedBy, boolean isEmployee) {
        requireCustomer(isEmployee);
        Account account = accountRepository.findRequiredActiveById(request.toIban());
        transactionPolicy.requireCallerOwnsAccount(account, callerUserId);
        account.credit(request.amount());
        return transactionMapper.toResponse(transactionRepository.record(
                null, request.toIban(), request.amount(),
                performedBy, request.description(), TransactionType.DEPOSIT
        ));
    }

    private TransactionResponse withdrawal(TransactionRequest request, Long callerUserId, String performedBy, boolean isEmployee) {
        requireCustomer(isEmployee);
        Account account = accountRepository.findRequiredActiveById(request.fromIban());
        transactionPolicy.requireCallerOwnsAccount(account, callerUserId);
        deductWithLimitChecks(account, request.amount());
        return transactionMapper.toResponse(transactionRepository.record(
                request.fromIban(), null, request.amount(),
                performedBy, request.description(), TransactionType.WITHDRAWAL
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findTransactions(TransactionFilter filter, Pageable pageable,
                                                       Long callerUserId, boolean isEmployee) {
        if (!isEmployee) {
            return transactionRepository.findByFilterForUser(filter, pageable, accountRepository.findOwnedIbansByUserId(callerUserId))
                    .map(transactionMapper::toResponse);
        }
        return transactionRepository.findByFilter(filter, pageable).map(transactionMapper::toResponse);
    }

    private void requireCustomer(boolean isEmployee) {
        if (isEmployee) {
            throw new UnauthorizedAccountAccessException("Deposits and withdrawals are only available to customers");
        }
    }

    private void deductWithLimitChecks(Account account, BigDecimal amount) {
        transactionPolicy.enforceAbsoluteLimit(account, amount);
        BigDecimal todaySpent = transactionRepository.sumOutgoingTodayForIban(account.getIban());
        transactionPolicy.enforceDailyLimit(account, amount, todaySpent);
        account.debit(amount);
    }
}
