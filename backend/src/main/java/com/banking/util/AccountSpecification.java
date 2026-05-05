package com.banking.util;

import com.banking.model.Account;
import com.banking.model.User;
import org.springframework.data.jpa.domain.Specification;

/** JPA Specification builders for dynamic account queries. */
public class AccountSpecification {

    /** @return spec restricting results to customer-owned accounts only */
    public static Specification<Account> customerAccountsOnly() {
        return (root, query, cb) -> cb.equal(root.get("user").get("role"), User.Role.CUSTOMER);
    }

    /** @return spec matching accounts whose owner email contains the given string (case-insensitive) */
    public static Specification<Account> ownerEmailContains(String email) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("user").get("email")),
                "%" + email.toLowerCase() + "%");
    }

    /** @return spec filtering accounts by their active flag */
    public static Specification<Account> isActive(boolean active) {
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }
}
