package com.banking.service.interfaces;

import com.banking.dto.TransactionFilter;
import com.banking.dto.TransactionRequest;
import com.banking.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITransactionService {
    TransactionResponse createTransaction(TransactionRequest request, Long callerUserId, String performedBy, boolean isEmployee);
    Page<TransactionResponse> findTransactions(TransactionFilter filter, Pageable pageable, Long callerUserId, boolean isEmployee);
}
