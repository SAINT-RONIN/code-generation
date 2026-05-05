package com.banking.exception;

/** Thrown when no customer exists for the given ID, or the user is not a CUSTOMER. */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer not found: " + id);
    }
}
