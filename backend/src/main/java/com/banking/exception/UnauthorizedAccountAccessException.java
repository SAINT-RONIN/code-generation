package com.banking.exception;

public class UnauthorizedAccountAccessException extends RuntimeException {
    public UnauthorizedAccountAccessException(String iban) {
        super("You do not have access to account: " + iban);
    }
}
