package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @Schema(description = "Source account IBAN", example = "NL91ABNA0417164300")
        @NotBlank String fromIban,
        @Schema(description = "Target account IBAN", example = "NL39RABO0300065264")
        @NotBlank String toIban,
        @Schema(description = "Transfer amount", example = "250.00")
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @Schema(description = "Optional transfer note", example = "Rent payment")
        String description
) {}
