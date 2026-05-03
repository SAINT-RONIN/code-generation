package com.banking.repository;

import com.banking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = 'CUSTOMER' AND u.accounts IS EMPTY")
    List<User> findAllPendingCustomers();

    @Query("SELECT u FROM User u WHERE u.role = 'CUSTOMER' AND u.accounts IS NOT EMPTY " +
           "AND LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) " +
           "AND LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<User> searchApprovedCustomersByName(@Param("firstName") String firstName, @Param("lastName") String lastName);
}
