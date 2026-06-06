package com.banking.service.interfaces;

import com.banking.dto.TransactionFilter;
import com.banking.dto.TransactionRequest;
import com.banking.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITransactionService {
    TransactionResponse createCustomerTransaction(TransactionRequest request, Long callerUserId, String performedBy);
    TransactionResponse createEmployeeTransfer(TransactionRequest request, String performedBy);
    Page<TransactionResponse> findCustomerTransactions(TransactionFilter filter, Pageable pageable, Long callerUserId);
    Page<TransactionResponse> findAllTransactions(TransactionFilter filter, Pageable pageable);
}
