package com.banking.service;

import com.banking.dto.TransactionFilter;
import com.banking.dto.TransactionRequest;
import com.banking.dto.TransactionResponse;
import com.banking.exception.InvalidTransferException;
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
        return switch (request.type()) {
            case TRANSFER -> transfer(request, callerUserId, performedBy, isEmployee);
            case DEPOSIT -> deposit(request, callerUserId, performedBy);
            case WITHDRAWAL -> withdrawal(request, callerUserId, performedBy);
        };
    }

    private TransactionResponse transfer(TransactionRequest request, Long callerUserId, String performedBy, boolean isEmployee) {
        validateNotBlank(request.fromIban(), "fromIban is required for transfers");
        validateNotBlank(request.toIban(), "toIban is required for transfers");
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

    private TransactionResponse deposit(TransactionRequest request, Long callerUserId, String performedBy) {
        validateNotBlank(request.toIban(), "toIban is required for deposits");
        Account account = accountRepository.findRequiredActiveById(request.toIban());
        transactionPolicy.requireCallerOwnsAccount(account, callerUserId);
        account.credit(request.amount());
        return transactionMapper.toResponse(transactionRepository.record(
                null, request.toIban(), request.amount(),
                performedBy, request.description(), TransactionType.DEPOSIT
        ));
    }

    private TransactionResponse withdrawal(TransactionRequest request, Long callerUserId, String performedBy) {
        validateNotBlank(request.fromIban(), "fromIban is required for withdrawals");
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

    private void validateNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidTransferException(message);
        }
    }

    private void deductWithLimitChecks(Account account, BigDecimal amount) {
        transactionPolicy.enforceAbsoluteLimit(account, amount);
        BigDecimal todaySpent = transactionRepository.sumOutgoingTodayForIban(account.getIban());
        transactionPolicy.enforceDailyLimit(account, amount, todaySpent);
        account.debit(amount);
    }
}
