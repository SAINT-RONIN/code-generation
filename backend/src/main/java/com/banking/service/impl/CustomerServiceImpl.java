package com.banking.service.impl;

import com.banking.dto.CustomerResponse;
import com.banking.dto.CustomerUpdateRequest;
import com.banking.exception.CustomerNotFoundException;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.service.interfaces.CustomerService;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public CustomerServiceImpl(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> findCustomers(UserStatus status, String search, Pageable pageable) {
        return userRepository.findCustomers(status, search, pageable)
                .map(CustomerResponse::from);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request) {
        User customer = findCustomerOrThrow(id);
        if (request.status() != null) {
            applyStatusTransition(customer, UserStatus.valueOf(request.status()), request);
        } else if (hasLimitUpdates(request)) {
            applyLimitUpdates(customer, request);
        }
        userRepository.save(customer);
        return CustomerResponse.from(customer);
    }

    private void applyStatusTransition(User customer, UserStatus newStatus, CustomerUpdateRequest request) {
        UserStatus current = customer.getStatus();
        if (newStatus == UserStatus.ACTIVE && current == UserStatus.PENDING) {
            approveCustomer(customer, request);
        } else if (newStatus == UserStatus.CLOSED) {
            closeCustomer(customer);
        } else if (newStatus == UserStatus.ACTIVE && current == UserStatus.CLOSED) {
            reopenCustomer(customer);
        } else {
            customer.setStatus(newStatus);
        }
    }

    private void approveCustomer(User customer, CustomerUpdateRequest request) {
        BigDecimal daily    = request.dailyLimit()    != null ? request.dailyLimit()    : BigDecimal.valueOf(2000);
        BigDecimal absolute = request.absoluteLimit() != null ? request.absoluteLimit() : BigDecimal.ZERO;
        accountRepository.save(buildAccount(AccountType.CHECKING, daily, absolute, customer));
        accountRepository.save(buildAccount(AccountType.SAVINGS,  daily, absolute, customer));
        customer.setStatus(UserStatus.ACTIVE);
    }

    private void closeCustomer(User customer) {
        accountRepository.findAllByUserEmail(customer.getEmail()).forEach(a -> a.setActive(false));
        customer.setStatus(UserStatus.CLOSED);
    }

    private void reopenCustomer(User customer) {
        accountRepository.findAllByUserEmail(customer.getEmail()).forEach(a -> a.setActive(true));
        customer.setStatus(UserStatus.ACTIVE);
    }

    private void applyLimitUpdates(User customer, CustomerUpdateRequest request) {
        accountRepository.findAllByUserEmail(customer.getEmail()).forEach(account -> {
            if (request.dailyLimit()    != null) account.setDailyLimit(request.dailyLimit());
            if (request.absoluteLimit() != null) account.setAbsoluteLimit(request.absoluteLimit());
        });
    }

    private boolean hasLimitUpdates(CustomerUpdateRequest request) {
        return request.dailyLimit() != null || request.absoluteLimit() != null;
    }

    private Account buildAccount(AccountType type, BigDecimal dailyLimit, BigDecimal absoluteLimit, User customer) {
        return new Account(generateUniqueIban(), type, absoluteLimit, dailyLimit, customer);
    }

    private String generateUniqueIban() {
        String iban;
        do {
            iban = Iban.random(CountryCode.NL).toString();
        } while (accountRepository.existsById(iban));
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
}
