package com.banking.exception;

public class CustomerAlreadyApprovedException extends RuntimeException {
    public CustomerAlreadyApprovedException(Long id) {
        super("Customer already has accounts: " + id);
    }
}
