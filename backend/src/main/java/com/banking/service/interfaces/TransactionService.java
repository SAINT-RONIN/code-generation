package com.banking.service.interfaces;

import com.banking.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponse transfer(TransferRequest request, String callerEmail, boolean isEmployee);
    TransactionResponse deposit(AtmRequest request, String callerEmail);
    TransactionResponse withdrawal(AtmRequest request, String callerEmail);
    Page<TransactionResponse> findTransactions(TransactionFilter filter, Pageable pageable, String callerEmail, boolean isEmployee);
}
