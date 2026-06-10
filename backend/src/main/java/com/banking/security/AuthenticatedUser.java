package com.banking.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/** Custom principal that adds our database user ID to Spring Security's User. */
public class AuthenticatedUser extends org.springframework.security.core.userdetails.User {

    private final Long id;

    public AuthenticatedUser(Long id, String email, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities) {
        super(email, password, enabled, true, true, true, authorities);
        this.id = id;
    }

    // Returns the database user ID
    public Long getId() {
        return id;
    }

    // Returns the user's email (stored as "username" in Spring Security)
    public String getEmail() {
        return getUsername();
    }

    // Checks if this user has the EMPLOYEE role
    public boolean isEmployee() {
        return getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }
}
