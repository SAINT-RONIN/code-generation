package com.banking.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Custom principal that extends Spring Security's User class with our database user ID.
 * This is stored in the SecurityContext after JWT validation so that controllers
 * can access the caller's ID, email, and role without extra database lookups.
 */
public class AuthenticatedUser extends org.springframework.security.core.userdetails.User {

    // The primary key of the User entity in our database
    private final Long id;

    /**
     * @param id          database user ID
     * @param email       used as the Spring Security "username"
     * @param password    hashed password (needed by Spring Security's internal checks)
     * @param enabled     false for PENDING/CLOSED users — blocks login even with correct credentials
     * @param authorities contains the single authority "ROLE_CUSTOMER" or "ROLE_EMPLOYEE"
     */
    public AuthenticatedUser(Long id, String email, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities) {
        // The four boolean flags (accountNonExpired, credentialsNonExpired, accountNonLocked)
        // are all set to true because we only use the "enabled" flag to control access
        super(email, password, enabled, true, true, true, authorities);
        this.id = id;
    }

    // Returns the internal database user ID stored in the authenticated principal
    public Long getId() {
        return id;
    }

    // Returns the user's email (Spring Security stores it as the "username" field)
    public String getEmail() {
        return getUsername();
    }

    // Checks whether this user holds the EMPLOYEE role by scanning their granted authorities
    public boolean isEmployee() {
        return getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }
}
