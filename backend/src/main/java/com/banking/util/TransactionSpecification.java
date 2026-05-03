package com.banking.util;

import com.banking.dto.TransactionFilter;
import com.banking.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;

public class TransactionSpecification {

    public static Specification<Transaction> involvesIban(String iban) {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("fromIban"), iban),
                cb.equal(root.get("toIban"), iban)
        );
    }

    public static Specification<Transaction> matchesFilter(TransactionFilter filter) {
        Specification<Transaction> spec = Specification.where(null);
        if (filter.getFrom() != null)
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("timestamp"), filter.getFrom().atStartOfDay()));
        if (filter.getTo() != null)
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("timestamp"), filter.getTo().atTime(LocalTime.MAX)));
        if (filter.getAmountEq() != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("amount"), filter.getAmountEq()));
        if (filter.getAmountLt() != null)
            spec = spec.and((r, q, cb) -> cb.lessThan(r.get("amount"), filter.getAmountLt()));
        if (filter.getAmountGt() != null)
            spec = spec.and((r, q, cb) -> cb.greaterThan(r.get("amount"), filter.getAmountGt()));
        if (filter.getIban() != null)
            spec = spec.and((r, q, cb) -> cb.or(
                    cb.equal(r.get("fromIban"), filter.getIban()),
                    cb.equal(r.get("toIban"), filter.getIban())
            ));
        return spec;
    }
}
