package com.banking.service.interfaces;

import com.banking.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Executes and queries financial transactions: transfers, deposits, and withdrawals. */
public interface TransactionService {

    // Employee transfers skip ownership and savings-account rules; customer transfers enforce both
    /** @return the recorded transaction */
    TransactionResponse transfer(TransferRequest request, String callerEmail, boolean isEmployee);

    /** @return the recorded deposit transaction */
    TransactionResponse deposit(AtmRequest request, String callerEmail);

    /** @return the recorded withdrawal transaction */
    TransactionResponse withdrawal(AtmRequest request, String callerEmail);

    // Employees see all transactions; customers only see transactions involving their own IBANs
    /** @return paginated transactions matching the filter, scoped to caller if customer */
    Page<TransactionResponse> findTransactions(TransactionFilter filter, Pageable pageable, String callerEmail, boolean isEmployee);
}
