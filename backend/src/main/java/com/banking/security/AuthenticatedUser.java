package com.banking.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AuthenticatedUser extends org.springframework.security.core.userdetails.User {

    private final Long id;

    public AuthenticatedUser(Long id, String email, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities) {
        super(email, password, enabled, true, true, true, authorities);
        this.id = id;
    }

    // Return the internal user id stored in the authenticated principal.
    public Long getId() {
        return id;
    }

    // Return the user's email used as the Spring Security username.
    public String getEmail() {
        return getUsername();
    }

    // Check whether this user holds the EMPLOYEE role.
    public boolean isEmployee() {
        return getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }
}
