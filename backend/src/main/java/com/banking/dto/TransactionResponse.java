package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String fromIban,
        String toIban,
        @Schema(example = "250.00")
        BigDecimal amount,
        LocalDateTime timestamp,
        String performedBy,
        String description,
        @Schema(example = "TRANSFER")
        String transactionType
) {}
