package com.banking.exception;

/** Thrown when transfer rules are violated, e.g. a savings account sending to an external account. */
public class InvalidTransferException extends RuntimeException {

    public InvalidTransferException(String reason) {
        super(reason);
    }
}
