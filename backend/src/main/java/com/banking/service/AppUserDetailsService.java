package com.banking.service;

import com.banking.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security's UserDetailsService implementation.
 * Called by the JwtAuthenticationFilter to load the user from the database during
 * token validation, and also used internally by Spring's AuthenticationManager.
 *
 * The "username" in our system is the user's email address.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Constructor injection of the user repository
    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by email and returns an AuthenticatedUser (our custom UserDetails)
     * containing the user's ID, email, hashed password, enabled flag, and roles.
     * Throws UsernameNotFoundException if no user exists with this email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findAuthenticatedUserByEmail(email);
    }
}
