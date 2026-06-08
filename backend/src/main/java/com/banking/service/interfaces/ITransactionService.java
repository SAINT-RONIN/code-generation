package com.banking.service.interfaces;

import com.banking.dto.TransactionFilter;
import com.banking.dto.TransactionRequest;
import com.banking.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contract for transaction operations: creating transactions and querying history.
 * Implemented by TransactionService.
 */
public interface ITransactionService {

    // Creates a customer transaction (transfer, deposit, or withdrawal) — type is auto-inferred from IBANs
    TransactionResponse createCustomerTransaction(TransactionRequest request, Long callerUserId, String performedBy);

    // Creates an employee transfer (checking-to-checking between different customers only)
    TransactionResponse createEmployeeTransfer(TransactionRequest request, String performedBy);

    // Returns paginated transaction history filtered to the customer's own accounts only
    Page<TransactionResponse> findCustomerTransactions(TransactionFilter filter, Pageable pageable, Long callerUserId);

    // Returns paginated transaction history for all accounts (employee-only, no ownership filter)
    Page<TransactionResponse> findAllTransactions(TransactionFilter filter, Pageable pageable);
}
