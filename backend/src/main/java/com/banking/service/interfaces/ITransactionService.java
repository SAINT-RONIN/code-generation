package com.banking.service.interfaces;

import com.banking.dto.AtmRequest;
import com.banking.dto.TransactionFilter;
import com.banking.dto.TransactionResponse;
import com.banking.dto.TransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITransactionService {
    TransactionResponse transfer(TransferRequest request, Long callerUserId, String performedBy, boolean isEmployee);
    TransactionResponse deposit(AtmRequest request, Long callerUserId, String performedBy);
    TransactionResponse withdrawal(AtmRequest request, Long callerUserId, String performedBy);
    Page<TransactionResponse> findTransactions(TransactionFilter filter, Pageable pageable, Long callerUserId, boolean isEmployee);
}
