package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CustomerUpdateRequest(
        @Schema(description = "New customer status", example = "ACTIVE")
        String status,
        @Schema(description = "Daily limit to apply to the customer's accounts", example = "2000.00")
        BigDecimal dailyLimit,
        @Schema(description = "Absolute limit to apply to the customer's accounts", example = "-500.00")
        BigDecimal absoluteLimit
) {}
