package com.banking.dto;

import java.math.BigDecimal;

public record CustomerUpdateRequest(
        String status,
        BigDecimal dailyLimit,
        BigDecimal absoluteLimit
) {}
