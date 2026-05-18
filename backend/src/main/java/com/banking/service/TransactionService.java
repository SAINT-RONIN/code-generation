package com.banking.service;

import com.banking.dto.*;
import com.banking.exception.*;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
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

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public TransactionResponse transfer(TransferRequest request, Long callerUserId, String performedBy, boolean isEmployee) {
        Account from = accountRepository.findRequiredActiveById(request.fromIban());
        Account to   = accountRepository.findRequiredActiveById(request.toIban());
        if (isEmployee) {
            verifyBothAreCheckingAccounts(from, to);
        } else {
            verifyCallerOwnsAccount(from, callerUserId);
            verifyTransferRules(from, to);
        }
        deductWithLimitChecks(from, request.amount());
        creditAccount(to, request.amount());
        return transactionRepository.recordResponse(
                request.fromIban(),
                request.toIban(),
                request.amount(),
                performedBy,
                request.description(),
                com.banking.model.Transaction.TransactionType.TRANSFER
        );
    }

    @Override
    public TransactionResponse deposit(AtmRequest request, Long callerUserId, String performedBy) {
        Account account = accountRepository.findRequiredActiveById(request.iban());
        verifyCallerOwnsAccount(account, callerUserId);
        creditAccount(account, request.amount());
        return transactionRepository.recordResponse(
                null,
                request.iban(),
                request.amount(),
                performedBy,
                request.description(),
                com.banking.model.Transaction.TransactionType.DEPOSIT
        );
    }

    @Override
    public TransactionResponse withdrawal(AtmRequest request, Long callerUserId, String performedBy) {
        Account account = accountRepository.findRequiredActiveById(request.iban());
        verifyCallerOwnsAccount(account, callerUserId);
        deductWithLimitChecks(account, request.amount());
        return transactionRepository.recordResponse(
                request.iban(),
                null,
                request.amount(),
                performedBy,
                request.description(),
                com.banking.model.Transaction.TransactionType.WITHDRAWAL
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findTransactions(TransactionFilter filter, Pageable pageable,
                                                       Long callerUserId, boolean isEmployee) {
        if (!isEmployee) {
            return transactionRepository.findResponsesForUser(filter, pageable, accountRepository.findOwnedIbansByUserId(callerUserId));
        }
        return transactionRepository.findResponses(filter, pageable);
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

    private void verifyCallerOwnsAccount(Account account, Long callerUserId) {
        if (!account.getUser().getId().equals(callerUserId)) {
            throw new UnauthorizedAccountAccessException(account.getIban());
        }
    }

    private void creditAccount(Account account, BigDecimal amount) {
        account.credit(amount);
    }
}
