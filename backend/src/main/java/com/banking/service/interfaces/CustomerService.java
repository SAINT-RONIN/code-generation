package com.banking.service.interfaces;

import com.banking.dto.CustomerResponse;
import com.banking.dto.CustomerUpdateRequest;
import com.banking.model.User.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Page<CustomerResponse> findCustomers(UserStatus status, String search, Pageable pageable);
    CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request);
}
