package com.banking.policy;

import com.banking.exception.InvalidTransferException;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import org.springframework.stereotype.Component;

/** Enforces domain rules for account management. */
@Component
public class AccountPolicy {

    public void requireActiveCustomer(User customer) {
        if (customer.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidTransferException("Cannot create accounts for a customer that is not active");
        }
    }
}
