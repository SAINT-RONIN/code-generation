package com.banking.exception;

/** Thrown when a registration email is already in use. */
public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("The email address '" + email + "' is already registered.");
    }
}
