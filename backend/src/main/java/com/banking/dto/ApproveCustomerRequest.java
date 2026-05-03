package com.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApproveCustomerRequest(
        @NotNull @DecimalMin("0") BigDecimal absoluteLimit,
        @NotNull @DecimalMin("0") BigDecimal dailyLimit
) {}
