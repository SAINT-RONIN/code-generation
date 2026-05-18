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

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return getUsername();
    }
}
