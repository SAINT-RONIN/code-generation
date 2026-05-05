package com.banking.dto;

import com.banking.model.Account;
import com.banking.model.User;

import java.math.BigDecimal;

public record AccountResponse(
        String iban,
        String accountType,
        BigDecimal balance,
        BigDecimal absoluteLimit,
        BigDecimal dailyLimit,
        boolean active,
        UserSummary user
) {
    public record UserSummary(Long id, String firstName, String lastName, String email) {}

    public static AccountResponse from(Account account) {
        User owner = account.getUser();
        return new AccountResponse(
                account.getIban(),
                account.getAccountType().name(),
                account.getBalance(),
                account.getAbsoluteLimit(),
                account.getDailyLimit(),
                account.isActive(),
                new UserSummary(owner.getId(), owner.getFirstName(), owner.getLastName(), owner.getEmail())
        );
    }
}
