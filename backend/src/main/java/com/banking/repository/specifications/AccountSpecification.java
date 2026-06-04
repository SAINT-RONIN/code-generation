package com.banking.repository.specifications;

import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import org.springframework.data.jpa.domain.Specification;

/** JPA Specification builders for dynamic account queries. */
public class AccountSpecification {

    public static Specification<Account> customerAccountsOnly() {
        return (root, query, cb) -> cb.equal(root.get("user").get("role"), User.Role.CUSTOMER);
    }

    public static Specification<Account> ownerEmailContains(String email) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("user").get("email")),
                "%" + email.toLowerCase() + "%");
    }

    public static Specification<Account> isActive(boolean active) {
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    public static Specification<Account> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Account> hasAccountType(AccountType accountType) {
        return (root, query, cb) -> cb.equal(root.get("accountType"), accountType);
    }
}
