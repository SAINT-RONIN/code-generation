package com.banking.service;

import com.banking.dto.TransactionFilter;
import com.banking.dto.TransactionRequest;
import com.banking.dto.TransactionResponse;
import com.banking.exception.DailyLimitExceededException;
import com.banking.exception.InsufficientFundsException;
import com.banking.exception.InvalidTransferException;
import com.banking.exception.UnauthorizedAccountAccessException;
import com.banking.mapper.TransactionMapper;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.Transaction.TransactionType;
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

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                              TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = transactionMapper;
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
        if (request.fromIban().equals(request.toIban())) {
            throw new InvalidTransferException("Source and destination account cannot be the same");
        }
        Account from = accountRepository.findRequiredActiveById(request.fromIban());
        Account to   = accountRepository.findRequiredActiveById(request.toIban());
        if (isEmployee) {
            verifyBothAreCheckingAccounts(from, to);
            verifyDifferentOwners(from, to);
        } else {
            verifyCallerOwnsAccount(from, callerUserId);
            verifyTransferRules(from, to);
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
        verifyCallerOwnsAccount(account, callerUserId);
        account.credit(request.amount());
        return transactionMapper.toResponse(transactionRepository.record(
                null, request.toIban(), request.amount(),
                performedBy, request.description(), TransactionType.DEPOSIT
        ));
    }

    private TransactionResponse withdrawal(TransactionRequest request, Long callerUserId, String performedBy) {
        validateNotBlank(request.fromIban(), "fromIban is required for withdrawals");
        Account account = accountRepository.findRequiredActiveById(request.fromIban());
        verifyCallerOwnsAccount(account, callerUserId);
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
        enforceAbsoluteLimit(account, amount);
        enforceDailyLimit(account, amount);
        account.debit(amount);
    }

    private void enforceAbsoluteLimit(Account account, BigDecimal amount) {
        if (account.getBalance().subtract(amount).compareTo(account.getAbsoluteLimit()) < 0) {
            throw new InsufficientFundsException(account.getIban());
        }
    }

    private void enforceDailyLimit(Account account, BigDecimal amount) {
        BigDecimal todaySpent = transactionRepository.sumOutgoingTodayForIban(account.getIban());
        if (todaySpent.add(amount).compareTo(account.getDailyLimit()) > 0) {
            throw new DailyLimitExceededException(account.getIban());
        }
    }

    private void verifyTransferRules(Account from, Account to) {
        boolean isOwnAccounts = from.getUser().getId().equals(to.getUser().getId());
        if (from.getAccountType() == AccountType.SAVINGS && !isOwnAccounts) {
            throw new InvalidTransferException("Savings accounts can only transfer to own accounts");
        }
        if (!isOwnAccounts && to.getAccountType() == AccountType.SAVINGS) {
            throw new InvalidTransferException("External transfers must target a checking account");
        }
    }

    private void verifyBothAreCheckingAccounts(Account from, Account to) {
        if (from.getAccountType() != AccountType.CHECKING || to.getAccountType() != AccountType.CHECKING) {
            throw new InvalidTransferException("Employee transfers must be between checking accounts");
        }
    }

    private void verifyDifferentOwners(Account from, Account to) {
        if (from.getUser().getId().equals(to.getUser().getId())) {
            throw new InvalidTransferException("Employee transfers must be between different customers");
        }
    }

    private void verifyCallerOwnsAccount(Account account, Long callerUserId) {
        if (!account.getUser().getId().equals(callerUserId)) {
            throw new UnauthorizedAccountAccessException(account.getIban());
        }
    }
}
