package com.banking.service.interfaces;

import com.banking.dto.AccountCreateRequest;
import com.banking.dto.AccountResponse;
import com.banking.dto.AccountUpdateRequest;
import com.banking.dto.IbanSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contract for bank account operations: searching, listing, creating, and updating accounts.
 * Implemented by AccountService.
 */
public interface IAccountService {

    // Searches for other customers' checking accounts by name or IBAN (for transfers)
    List<IbanSearchResponse> searchCustomerCheckingIbansByName(String firstName, String lastName, String iban, Long excludeUserId);

    // Returns the logged-in customer's own active accounts
    List<AccountResponse> findMyAccounts(Long userId);

    // Returns a paginated list of all accounts with optional filters (employee-only)
    Page<AccountResponse> findAllAccounts(String ownerEmail, Boolean active, Long userId, String accountType, Pageable pageable);

    // Creates a new checking or savings account for an existing customer (employee-only)
    AccountResponse createAccount(AccountCreateRequest request);

    // Updates an account's active status or limits (employee-only)
    AccountResponse updateAccount(String iban, AccountUpdateRequest request);
}
