package com.banking.exception;

/** Thrown when a debit would drop the balance below the account's absolute limit. */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String iban) {
        super("Insufficient funds for account: " + iban);
    }
}
