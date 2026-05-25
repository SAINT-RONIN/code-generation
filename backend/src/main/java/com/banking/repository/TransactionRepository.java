package com.banking.repository;

import com.banking.dto.TransactionFilter;
import com.banking.model.Transaction;
import com.banking.model.Transaction.TransactionType;
import com.banking.repository.specifications.TransactionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromIban = :iban AND t.timestamp >= :startOfToday")
    BigDecimal sumOutgoingTodayForIban(@Param("iban") String iban, @Param("startOfToday") LocalDateTime startOfToday);

    default BigDecimal sumOutgoingTodayForIban(String iban) {
        return sumOutgoingTodayForIban(iban, LocalDate.now().atStartOfDay());
    }

    default Transaction record(String fromIban, String toIban, BigDecimal amount,
                               String performedBy, String description, TransactionType type) {
        return save(new Transaction(fromIban, toIban, amount, performedBy, description, type));
    }

    default Page<Transaction> findByFilter(TransactionFilter filter, Pageable pageable) {
        return findAll(TransactionSpecification.matchesFilter(filter), pageable);
    }

    default Page<Transaction> findByFilterForUser(TransactionFilter filter, Pageable pageable, List<String> ibans) {
        if (ibans.isEmpty()) {
            return Page.empty(pageable);
        }
        Specification<Transaction> spec = TransactionSpecification.matchesFilter(filter)
                .and(involvesAnyOf(ibans));
        return findAll(spec, pageable);
    }

    private Specification<Transaction> involvesAnyOf(List<String> ibans) {
        return (root, query, cb) -> cb.or(
                root.get("fromIban").in(ibans),
                root.get("toIban").in(ibans)
        );
    }
}
