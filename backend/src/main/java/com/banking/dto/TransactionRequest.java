package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @Schema(description = "Source account IBAN (required for transfers and withdrawals)", example = "NL91ABNA0417164300")
        String fromIban,
        @Schema(description = "Target account IBAN (required for transfers and deposits)", example = "NL39RABO0300065264")
        String toIban,
        @Schema(description = "Transaction amount", example = "250.00")
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @Schema(description = "Optional note", example = "Rent payment")
        String description
) {}
