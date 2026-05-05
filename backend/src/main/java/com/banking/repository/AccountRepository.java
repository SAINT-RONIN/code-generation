package com.banking.repository;

import com.banking.model.Account;
import com.banking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String>, JpaSpecificationExecutor<Account> {
    Page<Account> findAllByUserRole(User.Role role, Pageable pageable);
    List<Account> findAllByUserEmailAndActiveTrue(String email);
    List<Account> findAllByUserEmail(String email);
}
