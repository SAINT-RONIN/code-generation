package com.banking.policy;

import com.banking.exception.InvalidTransferException;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import org.springframework.stereotype.Component;

/** Enforces business rules for account management operations. */
@Component
public class AccountPolicy {

    // Only ACTIVE customers can have new accounts created for them
    public void requireActiveCustomer(User customer) {
        if (customer.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidTransferException("Cannot create accounts for a customer that is not active");
        }
    }
}
