package com.banking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
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
        this.fromIban = fromIban;
        this.toIban = toIban;
        this.amount = amount;
        this.performedBy = performedBy;
        this.description = description;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();
    }
}
