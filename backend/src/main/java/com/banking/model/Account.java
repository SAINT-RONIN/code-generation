package com.banking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** A bank account owned by a customer; IBAN is the primary key. */
@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor
public class Account {

    @Id
    private String iban;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal absoluteLimit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyLimit = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum AccountType {
        CHECKING,
        SAVINGS
    }

    public Account(String iban, AccountType accountType, BigDecimal absoluteLimit, BigDecimal dailyLimit, User user) {
        this(iban, accountType, BigDecimal.ZERO, absoluteLimit, dailyLimit, user);
    }

    public Account(String iban, AccountType accountType, BigDecimal balance,
                   BigDecimal absoluteLimit, BigDecimal dailyLimit, User user) {
        this.iban = iban;
        this.accountType = accountType;
        this.balance = balance;
        this.absoluteLimit = absoluteLimit;
        this.dailyLimit = dailyLimit;
        this.user = user;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public void updateAbsoluteLimit(BigDecimal absoluteLimit) {
        this.absoluteLimit = absoluteLimit;
    }

    public void debit(BigDecimal amount) {
        this.balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.balance = balance.add(amount);
    }
}
