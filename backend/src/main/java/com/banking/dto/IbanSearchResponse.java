package com.banking.dto;

import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;

public record IbanSearchResponse(String firstName, String lastName, String checkingIban) {

    public static IbanSearchResponse from(User customer) {
        String iban = customer.getAccounts().stream()
                .filter(a -> a.getAccountType() == AccountType.CHECKING)
                .map(Account::getIban)
                .findFirst()
                .orElseThrow();
        return new IbanSearchResponse(customer.getFirstName(), customer.getLastName(), iban);
    }
}
