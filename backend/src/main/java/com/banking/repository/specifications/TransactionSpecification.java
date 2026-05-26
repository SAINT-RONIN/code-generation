package com.banking.repository.specifications;

import com.banking.dto.TransactionFilter;
import com.banking.model.Transaction;
import com.banking.model.Transaction.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;

/** JPA Specification builders for dynamic transaction queries. */
public class TransactionSpecification {

    /** @return a combined spec for all non-null fields in the filter */
    public static Specification<Transaction> matchesFilter(TransactionFilter filter) {
        Specification<Transaction> spec = Specification.where(null);

        // Date range: 'to' is inclusive up to end-of-day
        if (filter.getFrom() != null)
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("timestamp"), filter.getFrom().atStartOfDay()));
        if (filter.getTo() != null)
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("timestamp"), filter.getTo().atTime(LocalTime.MAX)));

        // Exact amount match
        if (filter.getAmountEq() != null)
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("amount"), filter.getAmountEq()));

        // Legacy lt/gt params (kept for API flexibility)
        if (filter.getAmountLt() != null)
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("amount"), filter.getAmountLt()));
        if (filter.getAmountGt() != null)
            spec = spec.and((root, query, cb) ->
                    cb.greaterThan(root.get("amount"), filter.getAmountGt()));

        // Frontend uses amountMin/amountMax (inclusive range)
        if (filter.getAmountMin() != null)
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("amount"), filter.getAmountMin()));
        if (filter.getAmountMax() != null)
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("amount"), filter.getAmountMax()));

        if (filter.getIban() != null)
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("fromIban"), filter.getIban()),
                    cb.equal(root.get("toIban"), filter.getIban())
            ));

        if (filter.getTransactionType() != null && !filter.getTransactionType().isBlank())
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("transactionType"), TransactionType.valueOf(filter.getTransactionType())));

        return spec;
    }
}
