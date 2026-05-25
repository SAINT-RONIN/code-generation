package com.banking.mapper;

import com.banking.dto.TransactionResponse;
import com.banking.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getFromIban(),
                transaction.getToIban(),
                transaction.getAmount(),
                transaction.getTimestamp(),
                transaction.getPerformedBy(),
                transaction.getDescription(),
                transaction.getTransactionType().name()
        );
    }
}
