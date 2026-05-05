package com.banking.service.interfaces;

import com.banking.dto.CustomerResponse;
import com.banking.dto.CustomerUpdateRequest;
import com.banking.model.User.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Manages customer lifecycle: listing, approval, closure, and reopening. */
public interface CustomerService {

    // Passing null for status returns customers of all statuses
    /** @return paginated customers filtered by status and/or search term */
    Page<CustomerResponse> findCustomers(UserStatus status, String search, Pageable pageable);

    // Status transitions: PENDING→ACTIVE creates accounts, ACTIVE→CLOSED deactivates them, CLOSED→ACTIVE reactivates them
    /** @return the updated customer */
    CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request);
}
