package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TransactionFilter {
    @Schema(description = "Start date filter", example = "2026-05-01")
    private LocalDate from;
    @Schema(description = "End date filter", example = "2026-05-31")
    private LocalDate to;
    private BigDecimal amountLt;
    private BigDecimal amountGt;
    private BigDecimal amountEq;
    private BigDecimal amountMin;
    private BigDecimal amountMax;
    @Schema(description = "Limit results to one IBAN", example = "NL91ABNA0417164300")
    private String iban;
    @Schema(description = "Filter by transaction type", example = "TRANSFER")
    private String transactionType;
}
