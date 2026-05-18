package com.banking.mapper;

import com.banking.dto.AccountResponse;
import com.banking.model.Account;
import com.banking.model.User;
import org.springframework.stereotype.Component;

@Component
// Keeps AccountResponse creation in one place so service code stays cleaner.
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        User owner = account.getUser();
        return new AccountResponse(
                account.getIban(),
                account.getAccountType().name(),
                account.getBalance(),
                account.getAbsoluteLimit(),
                account.getDailyLimit(),
                account.isActive(),
                new AccountResponse.UserSummary(
                        owner.getId(),
                        owner.getFirstName(),
                        owner.getLastName(),
                        owner.getEmail()
                )
        );
    }
}
