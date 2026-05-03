package com.banking.repository;

import com.banking.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromIban = :iban AND t.timestamp >= :startOfToday")
    BigDecimal sumOutgoingTodayForIban(@Param("iban") String iban, @Param("startOfToday") LocalDateTime startOfToday);

    @Query("SELECT t FROM Transaction t WHERE t.fromIban = :iban OR t.toIban = :iban")
    Page<Transaction> findAllByIban(@Param("iban") String iban, Pageable pageable);
}
