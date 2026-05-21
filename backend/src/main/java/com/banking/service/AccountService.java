package com.banking.service;

import com.banking.dto.AccountResponse;
import com.banking.dto.AccountUpdateRequest;
import com.banking.dto.IbanSearchResponse;
import com.banking.mapper.AccountMapper;
import com.banking.model.Account;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.service.interfaces.IAccountService;
import com.banking.util.AccountSpecification;
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

    public AccountService(UserRepository userRepository, AccountRepository accountRepository, AccountMapper accountMapper) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    // Find customer checking accounts by person name for transfers/search.
    @Override
    public List<IbanSearchResponse> searchCustomerCheckingIbansByName(String firstName, String lastName, String iban, Long excludeUserId) {
        String ibanParam = (iban == null || iban.isBlank()) ? null : iban;
        return userRepository.searchApprovedCustomersByName(firstName, lastName, ibanParam, excludeUserId);
    }

    // Get the signed-in user's active accounts.
    @Override
    public List<AccountResponse> findMyAccounts(Long userId) {
        return accountRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    // Get a filtered page of customer accounts for employee screens.
    @Override
    public Page<AccountResponse> findAllAccounts(String ownerEmail, Boolean active, Pageable pageable) {
        Specification<Account> spec = AccountSpecification.customerAccountsOnly();
        if (ownerEmail != null && !ownerEmail.isBlank()) {
            spec = spec.and(AccountSpecification.ownerEmailContains(ownerEmail));
        }
        if (active != null) {
            spec = spec.and(AccountSpecification.isActive(active));
        }
        return accountRepository.findAll(spec, pageable).map(accountMapper::toResponse);
    }

    // Update the selected account fields for one account.
    @Override
    @Transactional
    public AccountResponse updateAccount(String iban, AccountUpdateRequest request) {
        Account account = accountRepository.findRequiredById(iban);
        applyUpdate(account, request);
        return accountMapper.toResponse(account);
    }

    // Apply only the fields that were sent in the update request.
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
