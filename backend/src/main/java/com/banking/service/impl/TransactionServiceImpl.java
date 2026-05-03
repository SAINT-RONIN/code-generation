package com.banking.service.impl;

import com.banking.dto.*;
import com.banking.exception.*;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.Transaction;
import com.banking.model.Transaction.TransactionType;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.service.interfaces.TransactionService;
import com.banking.util.TransactionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public TransactionResponse transfer(TransferRequest request, String callerEmail) {
        Account from = findActiveAccountOrThrow(request.fromIban());
        Account to = findActiveAccountOrThrow(request.toIban());
        verifyCallerOwnsAccount(from, callerEmail);
        verifyTransferRules(from, to);
        deductWithLimitChecks(from, request.amount());
        to.setBalance(to.getBalance().add(request.amount()));
        return record(request.fromIban(), request.toIban(), request.amount(), callerEmail, request.description(), TransactionType.TRANSFER);
    }

    @Override
    public TransactionResponse employeeTransfer(TransferRequest request, String employeeEmail) {
        Account from = findActiveAccountOrThrow(request.fromIban());
        Account to = findActiveAccountOrThrow(request.toIban());
        verifyBothAreCheckingAccounts(from, to);
        deductWithLimitChecks(from, request.amount());
        to.setBalance(to.getBalance().add(request.amount()));
        return record(request.fromIban(), request.toIban(), request.amount(), employeeEmail, request.description(), TransactionType.TRANSFER);
    }

    @Override
    public TransactionResponse atmDeposit(AtmRequest request, String callerEmail) {
        Account account = findActiveAccountOrThrow(request.iban());
        verifyCallerOwnsAccount(account, callerEmail);
        account.setBalance(account.getBalance().add(request.amount()));
        return record(null, request.iban(), request.amount(), callerEmail, request.description(), TransactionType.DEPOSIT);
    }

    @Override
    public TransactionResponse atmWithdraw(AtmRequest request, String callerEmail) {
        Account account = findActiveAccountOrThrow(request.iban());
        verifyCallerOwnsAccount(account, callerEmail);
        deductWithLimitChecks(account, request.amount());
        return record(request.iban(), null, request.amount(), callerEmail, request.description(), TransactionType.WITHDRAWAL);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findTransactionsForIban(String iban, TransactionFilter filter, Pageable pageable, String callerEmail) {
        verifyCallerOwnsIban(iban, callerEmail);
        Specification<Transaction> spec = TransactionSpecification.involvesIban(iban)
                .and(TransactionSpecification.matchesFilter(filter));
        return transactionRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAllTransactions(TransactionFilter filter, Pageable pageable) {
        return transactionRepository.findAll(TransactionSpecification.matchesFilter(filter), pageable)
                .map(this::toResponse);
    }

    private void deductWithLimitChecks(Account account, BigDecimal amount) {
        enforceAbsoluteLimit(account, amount);
        enforceDailyLimit(account, amount);
        account.setBalance(account.getBalance().subtract(amount));
    }

    private void enforceAbsoluteLimit(Account account, BigDecimal amount) {
        if (account.getBalance().subtract(amount).compareTo(account.getAbsoluteLimit()) < 0) {
            throw new InsufficientFundsException(account.getIban());
        }
    }

    private void enforceDailyLimit(Account account, BigDecimal amount) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        BigDecimal todaySpent = transactionRepository.sumOutgoingTodayForIban(account.getIban(), startOfToday);
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

    private void verifyCallerOwnsAccount(Account account, String callerEmail) {
        if (!account.getUser().getEmail().equals(callerEmail)) {
            throw new UnauthorizedAccountAccessException(account.getIban());
        }
    }

    private void verifyCallerOwnsIban(String iban, String callerEmail) {
        Account account = findActiveAccountOrThrow(iban);
        verifyCallerOwnsAccount(account, callerEmail);
    }

    private Account findActiveAccountOrThrow(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException(iban));
        if (!account.isActive()) {
            throw new AccountNotFoundException(iban);
        }
        return account;
    }

    private TransactionResponse record(String fromIban, String toIban, BigDecimal amount,
                                       String performedBy, String description, TransactionType type) {
        Transaction tx = transactionRepository.save(new Transaction(fromIban, toIban, amount, performedBy, description, type));
        return toResponse(tx);
    }

    private TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(tx.getId(), tx.getFromIban(), tx.getToIban(), tx.getAmount(),
                tx.getTimestamp(), tx.getPerformedBy(), tx.getDescription(), tx.getTransactionType().name());
    }
}
