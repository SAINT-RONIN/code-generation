package com.banking.service.interfaces;

import com.banking.dto.AccountResponse;
import com.banking.dto.IbanSearchResponse;

import java.util.List;

public interface AccountService {
    List<IbanSearchResponse> searchCustomerCheckingIbansByName(String firstName, String lastName);
    List<AccountResponse> findMyAccounts(String email);
}
