package com.banking.exception;

/** Thrown when a customer attempts to operate on an account they do not own. */
public class UnauthorizedAccountAccessException extends RuntimeException {

    public UnauthorizedAccountAccessException(String iban) {
        super("You do not have access to account: " + iban);
    }
}
