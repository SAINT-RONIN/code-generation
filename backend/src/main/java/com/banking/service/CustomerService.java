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

    // Constructor injection of repositories and the DTO mapper
    public CustomerService(UserRepository userRepository, AccountRepository accountRepository, CustomerMapper customerMapper) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.customerMapper = customerMapper;
    }

    /**
     * Returns a paginated list of customers, optionally filtered by status and search text.
     * The JPQL query uses CAST(:search AS string) to handle null parameters on PostgreSQL
     * (PostgreSQL infers null JDBC params as bytea, which breaks the LOWER() function).
     */
    @Override
    @Transactional(readOnly = true) // Read-only transaction — optimizes database connection usage
    public Page<CustomerResponse> findCustomers(UserStatus status, String search, Pageable pageable) {
        return userRepository.findCustomers(status, search, pageable).map(customerMapper::toResponse);
    }

    /**
     * Updates a customer's status or account limits.
     * Handles multiple workflows:
     * - PENDING → ACTIVE: creates checking + savings accounts (approval)
     * - ACTIVE → CLOSED: deactivates all accounts
     * - CLOSED → ACTIVE: reactivates all accounts
     * - Limit updates: changes daily/absolute limits on all customer accounts
     */
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

    /**
     * Handles status transitions between PENDING, ACTIVE, and CLOSED.
     * Each transition has side effects (creating/activating/deactivating accounts).
     */
    private void applyStatusTransition(User customer, UserStatus newStatus, CustomerUpdateRequest request) {
        UserStatus current = customer.getStatus();
        if (newStatus == UserStatus.ACTIVE && current == UserStatus.PENDING) {
            // Approval: create the customer's first checking and savings accounts
            approveCustomer(customer, request);
        } else if (newStatus == UserStatus.CLOSED) {
            // Closing: mark all accounts as inactive so they can't be used for transactions
            updateAccountStatus(customer.getId(), false);
            customer.setStatus(UserStatus.CLOSED);
        } else if (newStatus == UserStatus.ACTIVE && current == UserStatus.CLOSED) {
            // Reactivation: re-enable all accounts
            updateAccountStatus(customer.getId(), true);
            customer.setStatus(UserStatus.ACTIVE);
        } else {
            // Direct status change (e.g. no side effects needed)
            customer.setStatus(newStatus);
        }
    }

    /**
     * Approves a pending customer by creating their initial checking and savings accounts
     * and setting their status to ACTIVE. Uses default limits if none were provided.
     */
    private void approveCustomer(User customer, CustomerUpdateRequest request) {
        // Use provided limits or fall back to sensible defaults
        BigDecimal daily    = request.dailyLimit()    != null ? request.dailyLimit()    : BigDecimal.valueOf(2000);
        BigDecimal absolute = request.absoluteLimit() != null ? request.absoluteLimit() : BigDecimal.ZERO;
        // Create one CHECKING and one SAVINGS account with unique IBANs
        accountRepository.saveAll(List.of(
                new Account(accountRepository.generateUniqueIban(), AccountType.CHECKING, absolute, daily, customer),
                new Account(accountRepository.generateUniqueIban(), AccountType.SAVINGS, absolute, daily, customer)
        ));
        customer.setStatus(UserStatus.ACTIVE);
    }

    // Activates or deactivates all accounts belonging to a customer
    private void updateAccountStatus(Long userId, boolean active) {
        accountRepository.updateActiveByUserId(userId, active);
    }

    // Updates the daily and absolute limits on all accounts for a customer
    private void applyLimitUpdates(User customer, CustomerUpdateRequest request) {
        accountRepository.updateLimitsByUserId(customer.getId(), request.dailyLimit(), request.absoluteLimit());
    }

    // Checks whether the update request contains any limit changes
    private boolean hasLimitUpdates(CustomerUpdateRequest request) {
        return request.dailyLimit() != null || request.absoluteLimit() != null;
    }
}
