package com.banking.service;

import com.banking.dto.AccountCreateRequest;
import com.banking.dto.AccountResponse;
import com.banking.dto.AccountUpdateRequest;
import com.banking.dto.IbanSearchResponse;
import com.banking.mapper.AccountMapper;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.policy.AccountPolicy;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.repository.specifications.AccountSpecification;
import com.banking.service.interfaces.IAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService implements IAccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountPolicy accountPolicy;

    public AccountService(UserRepository userRepository, AccountRepository accountRepository,
            AccountMapper accountMapper, AccountPolicy accountPolicy) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.accountPolicy = accountPolicy;
    }

    @Override
    public List<IbanSearchResponse> searchCustomerCheckingIbansByName(String firstName, String lastName, String iban,
            Long excludeUserId) {
        // Convert empty/blank IBAN to null so the JPQL query's CAST(:iban AS string) IS
        // NOT NULL check works
        String ibanParam = (iban == null || iban.isBlank()) ? null : iban;
        return userRepository.searchApprovedCustomersByName(firstName, lastName, ibanParam, excludeUserId);
    }

    @Override
    public List<AccountResponse> findMyAccounts(Long userId) {
        return accountRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    public Page<AccountResponse> findAllAccounts(String ownerEmail, Boolean active, Long userId, String accountType,
            Pageable pageable) {
        Specification<Account> spec = AccountSpecification.customerAccountsOnly();

        if (ownerEmail != null && !ownerEmail.isBlank()) {
            spec = spec.and(AccountSpecification.ownerEmailContains(ownerEmail));
        }
        if (active != null) {
            spec = spec.and(AccountSpecification.isActive(active));
        }
        if (userId != null) {
            spec = spec.and(AccountSpecification.hasUserId(userId));
        }
        if (accountType != null && !accountType.isBlank()) {
            spec = spec.and(AccountSpecification.hasAccountType(AccountType.valueOf(accountType)));
        }
        return accountRepository.findAll(spec, pageable).map(accountMapper::toResponse);
    }

    @Override
    @Transactional // Ensures the account creation is atomic
    public AccountResponse createAccount(AccountCreateRequest request) {
        User customer = userRepository.findRequiredCustomerById(request.customerId());
        accountPolicy.requireActiveCustomer(customer);
        AccountType type = AccountType.valueOf(request.accountType());
        Account account = accountRepository.save(
                new Account(accountRepository.generateUniqueIban(), type, request.absoluteLimit(), request.dailyLimit(),
                        customer));
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional // Ensures all updates are applied atomically
    public AccountResponse updateAccount(String iban, AccountUpdateRequest request) {
        Account account = accountRepository.findRequiredById(iban);
        applyUpdate(account, request);
        return accountMapper.toResponse(account);
    }

    // Applies only the fields that were sent in the update request (partial update
    // pattern)
    private void applyUpdate(Account account, AccountUpdateRequest request) {
        if (request.active() != null) {
            if (request.active()) {
                account.activate();
            } else {
                account.deactivate();
            }
        }
        if (request.dailyLimit() != null) {
            account.updateDailyLimit(request.dailyLimit());
        }
        if (request.absoluteLimit() != null) {
            account.updateAbsoluteLimit(request.absoluteLimit());
        }
    }
}
