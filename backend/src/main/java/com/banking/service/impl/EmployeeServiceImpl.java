package com.banking.service.impl;

import com.banking.dto.*;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.CustomerAlreadyApprovedException;
import com.banking.exception.CustomerNotFoundException;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.service.interfaces.EmployeeService;
import com.banking.util.IbanGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public EmployeeServiceImpl(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<PendingCustomerResponse> findPendingCustomers() {
        return userRepository.findAllPendingCustomers().stream()
                .map(this::toPendingCustomerResponse)
                .toList();
    }

    @Override
    @Transactional
    public void approveCustomerAndCreateAccounts(Long customerId, ApproveCustomerRequest request) {
        User customer = findCustomerOrThrow(customerId);
        if (!customer.getAccounts().isEmpty()) {
            throw new CustomerAlreadyApprovedException(customerId);
        }
        accountRepository.save(buildAccount(AccountType.CHECKING, request, customer));
        accountRepository.save(buildAccount(AccountType.SAVINGS, request, customer));
    }

    @Override
    public Page<AccountResponse> findAllCustomerAccounts(Pageable pageable) {
        return accountRepository.findAllByUserRole(User.Role.CUSTOMER, pageable)
                .map(this::toAccountResponse);
    }

    @Override
    @Transactional
    public void updateAccountLimits(String iban, UpdateLimitsRequest request) {
        Account account = findAccountOrThrow(iban);
        account.setAbsoluteLimit(request.absoluteLimit());
        account.setDailyLimit(request.dailyLimit());
    }

    @Override
    @Transactional
    public void closeAccount(String iban) {
        Account account = findAccountOrThrow(iban);
        account.setActive(false);
    }

    private Account buildAccount(AccountType type, ApproveCustomerRequest request, User customer) {
        String iban = generateUniqueIban();
        return new Account(iban, type, request.absoluteLimit(), request.dailyLimit(), customer);
    }

    private String generateUniqueIban() {
        String iban;
        do {
            iban = IbanGenerator.generate();
        } while (accountRepository.existsByIban(iban));
        return iban;
    }

    private User findCustomerOrThrow(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        if (user.getRole() != User.Role.CUSTOMER) {
            throw new CustomerNotFoundException(id);
        }
        return user;
    }

    private Account findAccountOrThrow(String iban) {
        return accountRepository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException(iban));
    }

    private PendingCustomerResponse toPendingCustomerResponse(User user) {
        return new PendingCustomerResponse(
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getBsn(), user.getPhoneNumber()
        );
    }

    private AccountResponse toAccountResponse(Account account) {
        User owner = account.getUser();
        return new AccountResponse(
                account.getId(), account.getIban(), account.getAccountType().name(),
                account.getBalance(), account.getAbsoluteLimit(), account.getDailyLimit(),
                account.isActive(), owner.getFirstName() + " " + owner.getLastName(), owner.getEmail()
        );
    }
}
