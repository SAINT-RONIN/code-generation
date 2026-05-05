package com.banking.exception;

/** Thrown when an account IBAN does not exist or the account is inactive. */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String iban) {
        super("Account not found: " + iban);
    }
}
