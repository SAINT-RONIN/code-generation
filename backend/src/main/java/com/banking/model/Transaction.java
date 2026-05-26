package com.banking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** An immutable record of a single financial movement (transfer, deposit, or withdrawal). */
@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromIban;

    private String toIban;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    private LocalDateTime timestamp;

    private String performedBy;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    public enum TransactionType {
        TRANSFER,
        DEPOSIT,
        WITHDRAWAL
    }

    public Transaction(String fromIban, String toIban, BigDecimal amount,
                       String performedBy, String description, TransactionType transactionType) {
        this(fromIban, toIban, amount, performedBy, description, transactionType, LocalDateTime.now());
    }

    public Transaction(String fromIban, String toIban, BigDecimal amount,
                       String performedBy, String description, TransactionType transactionType,
                       LocalDateTime timestamp) {
        this.fromIban = fromIban;
        this.toIban = toIban;
        this.amount = amount;
        this.performedBy = performedBy;
        this.description = description;
        this.transactionType = transactionType;
        this.timestamp = timestamp;
    }
}
