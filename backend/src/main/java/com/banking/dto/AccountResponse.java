package com.banking.dto;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String iban,
        String accountType,
        BigDecimal balance,
        BigDecimal absoluteLimit,
        BigDecimal dailyLimit,
        boolean active,
        String ownerName,
        String ownerEmail
) {}
