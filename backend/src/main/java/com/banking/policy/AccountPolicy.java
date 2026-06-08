package com.banking.policy;

import com.banking.exception.InvalidTransferException;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import org.springframework.stereotype.Component;

/**
 * Enforces business rules for account management operations.
 * Separated from the service layer to keep validation logic clean and testable.
 */
@Component // Registered as a Spring bean so it can be injected into AccountService
public class AccountPolicy {

    /**
     * Ensures that a customer is ACTIVE before creating new accounts for them.
     * PENDING customers need to be approved first; CLOSED customers need to be reactivated.
     */
    public void requireActiveCustomer(User customer) {
        if (customer.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidTransferException("Cannot create accounts for a customer that is not active");
        }
    }
}
