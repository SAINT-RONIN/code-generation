package com.banking.dto;

import java.math.BigDecimal;

public record AccountUpdateRequest(
        Boolean active,
        BigDecimal dailyLimit,
        BigDecimal absoluteLimit
) {}
