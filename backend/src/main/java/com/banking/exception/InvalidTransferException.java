package com.banking.exception;

public class InvalidTransferException extends RuntimeException {
    public InvalidTransferException(String reason) {
        super(reason);
    }
}
