package com.banking.repository;

import com.banking.dto.IbanSearchResponse;
import com.banking.exception.EmailAlreadyInUseException;
import com.banking.exception.CustomerNotFoundException;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT new com.banking.dto.IbanSearchResponse(u.firstName, u.lastName, a.iban) " +
           "FROM User u JOIN u.accounts a " +
           "WHERE u.role = 'CUSTOMER' " +
           "AND u.status = 'ACTIVE' " +
           "AND a.accountType = com.banking.model.Account$AccountType.CHECKING " +
           "AND u.id <> :excludeUserId " +
           "AND (" +
           "  (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) " +
           "   AND LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) " +
           "  OR (:iban IS NOT NULL AND LOWER(a.iban) LIKE LOWER(CONCAT('%', :iban, '%'))))")
    List<IbanSearchResponse> searchApprovedCustomersByName(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("iban") String iban,
            @Param("excludeUserId") Long excludeUserId);

    @Query("SELECT u FROM User u WHERE u.role = 'CUSTOMER' " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findCustomers(@Param("status") UserStatus status, @Param("search") String search, Pageable pageable);

    default void ensureEmailAvailable(String email) {
        if (existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }
    }

    default User findRequiredByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
    }

    default User findRequiredById(Long userId) {
        return findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Invalid user"));
    }

    default User findRequiredCustomerById(Long userId) {
        User user = findById(userId)
                .orElseThrow(() -> new CustomerNotFoundException(userId));
        if (user.getRole() != User.Role.CUSTOMER) {
            throw new CustomerNotFoundException(userId);
        }
        return user;
    }

    default User create(User user) {
        return save(user);
    }

    default UserDetails findAuthenticatedUserByEmail(String email) {
        User user = findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        boolean enabled = user.getStatus() == UserStatus.ACTIVE;
        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                enabled,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
