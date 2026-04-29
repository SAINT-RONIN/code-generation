package com.banking.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("The email address '" + email + "' is already registered.");
    }
}
