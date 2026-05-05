package com.banking.exception;

/** Thrown when a transaction would push today's outgoing spend past the account's daily limit. */
public class DailyLimitExceededException extends RuntimeException {

    public DailyLimitExceededException(String iban) {
        super("Daily transfer limit exceeded for account: " + iban);
    }
}
