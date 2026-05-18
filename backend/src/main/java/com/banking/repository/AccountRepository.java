package com.banking.repository;

import com.banking.exception.AccountNotFoundException;
import com.banking.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String>, JpaSpecificationExecutor<Account> {

    // Get one account by IBAN.
    @Override
    @EntityGraph(attributePaths = "user")
    Optional<Account> findById(String iban);

    // Get a page of accounts based on filters.
    @Override
    @EntityGraph(attributePaths = "user")
    Page<Account> findAll(Specification<Account> spec, Pageable pageable);

    // Get all active accounts for one user.
    @EntityGraph(attributePaths = "user")
    List<Account> findByUserIdAndActiveTrue(Long userId);

    // Get only the IBANs that belong to one user.
    @Query("select a.iban from Account a where a.user.id = :userId")
    List<String> findOwnedIbansByUserId(@Param("userId") Long userId);

    // Set active or inactive for all accounts of one user.
    @Modifying
    @Query("update Account a set a.active = :active where a.user.id = :userId")
    void updateActiveByUserId(@Param("userId") Long userId, @Param("active") boolean active);

    // Update account limits for one user's accounts.
    @Modifying
    @Query("update Account a set a.dailyLimit = coalesce(:dailyLimit, a.dailyLimit), " +
           "a.absoluteLimit = coalesce(:absoluteLimit, a.absoluteLimit) where a.user.id = :userId")
    void updateLimitsByUserId(@Param("userId") Long userId,
                              @Param("dailyLimit") BigDecimal dailyLimit,
                              @Param("absoluteLimit") BigDecimal absoluteLimit);

    default Account findRequiredById(String iban) {
        return findById(iban).orElseThrow(() -> new AccountNotFoundException(iban));
    }

    default Account findRequiredActiveById(String iban) {
        Account account = findRequiredById(iban);
        if (!account.isActive()) {
            throw new AccountNotFoundException(iban);
        }
        return account;
    }
}
