package com.banking.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TransactionFilter {
    private LocalDate from;
    private LocalDate to;
    private BigDecimal amountLt;
    private BigDecimal amountGt;
    private BigDecimal amountEq;
    private String iban;
}
