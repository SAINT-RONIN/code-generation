package com.banking.service.impl;

import com.banking.dto.AccountResponse;
import com.banking.dto.AccountUpdateRequest;
import com.banking.dto.IbanSearchResponse;
import com.banking.exception.AccountNotFoundException;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.service.interfaces.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AccountServiceImpl(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<IbanSearchResponse> searchCustomerCheckingIbansByName(String firstName, String lastName) {
        return userRepository.searchApprovedCustomersByName(firstName, lastName)
                .stream()
                .filter(customer -> customer.getAccounts().stream().anyMatch(a -> a.getAccountType() == AccountType.CHECKING))
                .map(this::mapUserToIbanSearchResponse)
                .toList();
    }

    @Override
    public List<AccountResponse> findMyAccounts(String email) {
        return accountRepository.findAllByUserEmailAndActiveTrue(email).stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Override
    public Page<AccountResponse> findAllAccounts(String ownerEmail, Boolean active, Pageable pageable) {
        Specification<Account> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("user").get("role"), User.Role.CUSTOMER));
        if (ownerEmail != null && !ownerEmail.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("user").get("email")),
                            "%" + ownerEmail.toLowerCase() + "%"));
        }
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        return accountRepository.findAll(spec, pageable).map(AccountResponse::from);
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(String iban, AccountUpdateRequest request) {
        Account account = accountRepository.findById(iban)
                .orElseThrow(() -> new AccountNotFoundException(iban));
        if (request.active() != null) account.setActive(request.active());
        if (request.dailyLimit() != null) account.setDailyLimit(request.dailyLimit());
        if (request.absoluteLimit() != null) account.setAbsoluteLimit(request.absoluteLimit());
        accountRepository.save(account);
        return AccountResponse.from(account);
    }

    private IbanSearchResponse mapUserToIbanSearchResponse(User customer) {
        String checkingIban = customer.getAccounts().stream()
                .filter(account -> account.getAccountType() == AccountType.CHECKING)
                .map(Account::getIban)
                .findFirst()
                .orElseThrow();
        return new IbanSearchResponse(customer.getFirstName(), customer.getLastName(), checkingIban);
    }
}
