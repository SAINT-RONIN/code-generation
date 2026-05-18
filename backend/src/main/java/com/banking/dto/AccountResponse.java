package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record AccountResponse(
        @Schema(example = "NL91ABNA0417164300")
        String iban,
        @Schema(example = "CHECKING")
        String accountType,
        @Schema(example = "1500.00")
        BigDecimal balance,
        @Schema(example = "-500.00")
        BigDecimal absoluteLimit,
        @Schema(example = "2000.00")
        BigDecimal dailyLimit,
        boolean active,
        UserSummary user
) {
    public record UserSummary(
            Long id,
            String firstName,
            String lastName,
            @Schema(example = "john@example.com")
            String email
    ) {}
}
