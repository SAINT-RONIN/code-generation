package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AccountUpdateRequest(
        @Schema(description = "Whether the account should be active")
        Boolean active,
        @Schema(description = "New daily limit", example = "2000.00")
        @PositiveOrZero BigDecimal dailyLimit,
        @Schema(description = "New absolute limit", example = "-500.00")
        @NegativeOrZero BigDecimal absoluteLimit
) {}
