package com.banking.service.interfaces;

import com.banking.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponse transfer(TransferRequest request, String callerEmail);
    TransactionResponse employeeTransfer(TransferRequest request, String employeeEmail);
    TransactionResponse atmDeposit(AtmRequest request, String callerEmail);
    TransactionResponse atmWithdraw(AtmRequest request, String callerEmail);
    Page<TransactionResponse> findTransactionsForIban(String iban, TransactionFilter filter, Pageable pageable, String callerEmail);
    Page<TransactionResponse> findAllTransactions(TransactionFilter filter, Pageable pageable);
}
