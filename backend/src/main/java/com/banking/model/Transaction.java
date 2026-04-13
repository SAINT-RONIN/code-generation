package com.banking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromIban;

    private String toIban;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    private LocalDateTime timestamp;

    private String performedBy; // email of user who initiated the transaction

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    public enum TransactionType {
        TRANSFER,
        DEPOSIT,
        WITHDRAWAL
    }

    // Constructors
    public Transaction() {}

    public Transaction(String fromIban, String toIban, BigDecimal amount, String performedBy, String description, TransactionType transactionType) {
        this.fromIban = fromIban;
        this.toIban = toIban;
        this.amount = amount;
        this.performedBy = performedBy;
        this.description = description;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromIban() { return fromIban; }
    public void setFromIban(String fromIban) { this.fromIban = fromIban; }

    public String getToIban() { return toIban; }
    public void setToIban(String toIban) { this.toIban = toIban; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
}
