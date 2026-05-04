package com.banking.dto;

import com.banking.model.Account;
import com.banking.model.User;

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
) {
    public static AccountResponse from(Account account) {
        User owner = account.getUser();
        return new AccountResponse(
                account.getId(), account.getIban(), account.getAccountType().name(),
                account.getBalance(), account.getAbsoluteLimit(), account.getDailyLimit(),
                account.isActive(), owner.getFirstName() + " " + owner.getLastName(), owner.getEmail()
        );
    }
}
