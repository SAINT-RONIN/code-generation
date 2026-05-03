package com.banking.exception;

public class DailyLimitExceededException extends RuntimeException {
    public DailyLimitExceededException(String iban) {
        super("Daily transfer limit exceeded for account: " + iban);
    }
}
