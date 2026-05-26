package com.banking.service;

import com.banking.dto.CustomerResponse;
import com.banking.dto.CustomerUpdateRequest;
import com.banking.mapper.CustomerMapper;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.service.interfaces.ICustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerService implements ICustomerService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(UserRepository userRepository, AccountRepository accountRepository, CustomerMapper customerMapper) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> findCustomers(UserStatus status, String search, Pageable pageable) {
        return userRepository.findCustomers(status, search, pageable).map(customerMapper::toResponse);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request) {
        User customer = userRepository.findRequiredCustomerById(id);
        if (request.status() != null) {
            applyStatusTransition(customer, UserStatus.valueOf(request.status()), request);
        } else if (hasLimitUpdates(request)) {
            applyLimitUpdates(customer, request);
        }
        return customerMapper.toResponse(customer);
    }

    private void applyStatusTransition(User customer, UserStatus newStatus, CustomerUpdateRequest request) {
        UserStatus current = customer.getStatus();
        if (newStatus == UserStatus.ACTIVE && current == UserStatus.PENDING) {
            approveCustomer(customer, request);
        } else if (newStatus == UserStatus.CLOSED) {
            updateAccountStatus(customer.getId(), false);
            customer.setStatus(UserStatus.CLOSED);
        } else if (newStatus == UserStatus.ACTIVE && current == UserStatus.CLOSED) {
            updateAccountStatus(customer.getId(), true);
            customer.setStatus(UserStatus.ACTIVE);
        } else {
            customer.setStatus(newStatus);
        }
    }

    private void approveCustomer(User customer, CustomerUpdateRequest request) {
        BigDecimal daily    = request.dailyLimit()    != null ? request.dailyLimit()    : BigDecimal.valueOf(2000);
        BigDecimal absolute = request.absoluteLimit() != null ? request.absoluteLimit() : BigDecimal.ZERO;
        accountRepository.saveAll(List.of(
                new Account(accountRepository.generateUniqueIban(), AccountType.CHECKING, absolute, daily, customer),
                new Account(accountRepository.generateUniqueIban(), AccountType.SAVINGS, absolute, daily, customer)
        ));
        customer.setStatus(UserStatus.ACTIVE);
    }

    private void updateAccountStatus(Long userId, boolean active) {
        accountRepository.updateActiveByUserId(userId, active);
    }

    private void applyLimitUpdates(User customer, CustomerUpdateRequest request) {
        accountRepository.updateLimitsByUserId(customer.getId(), request.dailyLimit(), request.absoluteLimit());
    }

    private boolean hasLimitUpdates(CustomerUpdateRequest request) {
        return request.dailyLimit() != null || request.absoluteLimit() != null;
    }
}
