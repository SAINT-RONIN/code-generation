package com.banking.exception;

/** Thrown when a caller attempts an operation they are not allowed to perform. */
public class UnauthorizedAccountAccessException extends RuntimeException {

    public UnauthorizedAccountAccessException(String message) {
        super(message);
    }
}
