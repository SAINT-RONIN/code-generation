package com.banking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
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

    // Constructors
    public Account() {}

    public Account(String iban, AccountType accountType, BigDecimal balance, BigDecimal absoluteLimit, BigDecimal dailyLimit, User user) {
        this.iban = iban;
        this.accountType = accountType;
        this.balance = balance;
        this.absoluteLimit = absoluteLimit;
        this.dailyLimit = dailyLimit;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getAbsoluteLimit() { return absoluteLimit; }
    public void setAbsoluteLimit(BigDecimal absoluteLimit) { this.absoluteLimit = absoluteLimit; }

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
