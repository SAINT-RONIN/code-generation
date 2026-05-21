package com.banking.service.interfaces;

import com.banking.dto.AccountResponse;
import com.banking.dto.AccountUpdateRequest;
import com.banking.dto.IbanSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAccountService {
    List<IbanSearchResponse> searchCustomerCheckingIbansByName(String firstName, String lastName, String iban, Long excludeUserId);
    List<AccountResponse> findMyAccounts(Long userId);
    Page<AccountResponse> findAllAccounts(String ownerEmail, Boolean active, Pageable pageable);
    AccountResponse updateAccount(String iban, AccountUpdateRequest request);
}
