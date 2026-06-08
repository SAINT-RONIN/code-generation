package com.banking.service.interfaces;

import com.banking.dto.CustomerResponse;
import com.banking.dto.CustomerUpdateRequest;
import com.banking.model.User.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contract for customer management operations (employee-only).
 * Implemented by CustomerService.
 */
public interface ICustomerService {

    // Returns a paginated list of customers, optionally filtered by status and search text
    Page<CustomerResponse> findCustomers(UserStatus status, String search, Pageable pageable);

    // Updates a customer's status (approve/close/reactivate) or account limits
    CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request);
}
