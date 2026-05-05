package com.banking.service.interfaces;

import com.banking.dto.AccountResponse;
import com.banking.dto.AccountUpdateRequest;
import com.banking.dto.IbanSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Manages account retrieval, search, and limit/status updates. */
public interface AccountService {

    // Only returns customers who have at least one CHECKING account
    /** @return list of customers whose name matches, with their checking IBAN */
    List<IbanSearchResponse> searchCustomerCheckingIbansByName(String firstName, String lastName);

    /** @return all active accounts belonging to the authenticated customer */
    List<AccountResponse> findMyAccounts(String email);

    /** @return paginated accounts for all customers, optionally filtered by owner email or active status */
    Page<AccountResponse> findAllAccounts(String ownerEmail, Boolean active, Pageable pageable);

    /** @return the updated account */
    AccountResponse updateAccount(String iban, AccountUpdateRequest request);
}
