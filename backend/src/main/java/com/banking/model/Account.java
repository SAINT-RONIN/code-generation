package com.banking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** A bank account owned by a customer; IBAN is the primary key. */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    private String iban;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal absoluteLimit = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal dailyLimit = BigDecimal.ZERO;

    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public enum AccountType {
        CHECKING,
        SAVINGS
    }

    public Account(String iban, AccountType accountType, BigDecimal absoluteLimit, BigDecimal dailyLimit, User user) {
        this.iban = iban;
        this.accountType = accountType;
        this.absoluteLimit = absoluteLimit;
        this.dailyLimit = dailyLimit;
        this.user = user;
    }
}
