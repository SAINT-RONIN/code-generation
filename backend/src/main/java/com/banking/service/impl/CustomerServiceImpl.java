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
            UserStatus newStatus = UserStatus.valueOf(request.status());
            UserStatus oldStatus = customer.getStatus();

            if (newStatus == UserStatus.ACTIVE && oldStatus == UserStatus.PENDING) {
                // Approve: create accounts
                BigDecimal dailyLimit = request.dailyLimit() != null ? request.dailyLimit() : BigDecimal.valueOf(2000);
                BigDecimal absoluteLimit = request.absoluteLimit() != null ? request.absoluteLimit() : BigDecimal.ZERO;
                accountRepository.save(buildAccount(AccountType.CHECKING, dailyLimit, absoluteLimit, customer));
                accountRepository.save(buildAccount(AccountType.SAVINGS, dailyLimit, absoluteLimit, customer));
                customer.setStatus(UserStatus.ACTIVE);
            } else if (newStatus == UserStatus.CLOSED && (oldStatus == UserStatus.ACTIVE || oldStatus == UserStatus.PENDING)) {
                // Close: deactivate all accounts
                accountRepository.findAllByUserEmail(customer.getEmail())
                        .forEach(a -> a.setActive(false));
                customer.setStatus(UserStatus.CLOSED);
            } else if (newStatus == UserStatus.ACTIVE && oldStatus == UserStatus.CLOSED) {
                // Reopen: reactivate all accounts
                accountRepository.findAllByUserEmail(customer.getEmail())
                        .forEach(a -> a.setActive(true));
                customer.setStatus(UserStatus.ACTIVE);
            } else {
                customer.setStatus(newStatus);
            }
        }

        // Update limits on existing accounts if provided (for non-PENDING transitions)
        if ((request.dailyLimit() != null || request.absoluteLimit() != null)
                && request.status() == null) {
            accountRepository.findAllByUserEmail(customer.getEmail()).forEach(a -> {
                if (request.dailyLimit() != null) a.setDailyLimit(request.dailyLimit());
                if (request.absoluteLimit() != null) a.setAbsoluteLimit(request.absoluteLimit());
            });
        }

        userRepository.save(customer);
        return CustomerResponse.from(customer);
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
