package com.banking.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String iban) {
        super("Insufficient funds for account: " + iban);
    }
}
