package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AccountCreateRequest(
        @Schema(description = "ID of the customer who will own the account", example = "2")
        @NotNull Long customerId,
        @Schema(description = "Account type: CHECKING or SAVINGS", example = "CHECKING")
        @NotNull String accountType,
        @Schema(description = "Daily transfer limit", example = "2000.00")
        @NotNull @PositiveOrZero BigDecimal dailyLimit,
        @Schema(description = "Overdraft floor — how far below zero the balance may drop (0 means no overdraft)", example = "0.00")
        @NotNull BigDecimal absoluteLimit
) {}
