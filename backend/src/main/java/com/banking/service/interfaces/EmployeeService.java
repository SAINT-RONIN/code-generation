package com.banking.service.interfaces;

import com.banking.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
    List<PendingCustomerResponse> findPendingCustomers();
    void approveCustomerAndCreateAccounts(Long customerId, ApproveCustomerRequest request);
    Page<AccountResponse> findAllCustomerAccounts(Pageable pageable);
    void updateAccountLimits(String iban, UpdateLimitsRequest request);
    void closeAccount(String iban);
}
