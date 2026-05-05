package com.banking.dto;

import com.banking.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String fromIban,
        String toIban,
        BigDecimal amount,
        LocalDateTime timestamp,
        String performedBy,
        String description,
        String transactionType
) {
    public static TransactionResponse from(Transaction transaction) {
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
