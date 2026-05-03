package com.banking.dto;

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
) {}
