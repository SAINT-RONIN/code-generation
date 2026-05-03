package com.banking.util;

import com.banking.dto.TransactionFilter;
import com.banking.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;

public class TransactionSpecification {

    public static Specification<Transaction> involvesIban(String iban) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.equal(root.get("fromIban"), iban),
                criteriaBuilder.equal(root.get("toIban"), iban)
        );
    }

    public static Specification<Transaction> matchesFilter(TransactionFilter filter) {
        Specification<Transaction> spec = Specification.where(null);
        if (filter.getFrom() != null)
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), filter.getFrom().atStartOfDay()));
        if (filter.getTo() != null)
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), filter.getTo().atTime(LocalTime.MAX)));
        if (filter.getAmountEq() != null)
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("amount"), filter.getAmountEq()));
        if (filter.getAmountLt() != null)
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("amount"), filter.getAmountLt()));
        if (filter.getAmountGt() != null)
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThan(root.get("amount"), filter.getAmountGt()));
        if (filter.getIban() != null)
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("fromIban"), filter.getIban()),
                    criteriaBuilder.equal(root.get("toIban"), filter.getIban())
            ));
        return spec;
    }
}
