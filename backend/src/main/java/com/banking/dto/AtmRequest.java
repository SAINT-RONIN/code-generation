package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AtmRequest(
        @Schema(description = "Target account IBAN", example = "NL91ABNA0417164300")
        @NotBlank String iban,
        @Schema(description = "Cash amount", example = "100.00")
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @Schema(description = "Optional note for the transaction", example = "ATM cash deposit")
        String description
) {}
