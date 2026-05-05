package com.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateLimitsRequest(
        @NotNull BigDecimal absoluteLimit,
        @NotNull @DecimalMin("0") BigDecimal dailyLimit
) {}
