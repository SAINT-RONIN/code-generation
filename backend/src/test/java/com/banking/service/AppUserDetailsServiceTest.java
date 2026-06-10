package com.banking.service;

import com.banking.repository.UserRepository;
import com.banking.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    // Active user is loaded as enabled with the correct role.
    @Test
    void loadsByEmailAndReturnsEnabledForActiveUser() {
        AuthenticatedUser activeUser = new AuthenticatedUser(1L, "john@test.com", "hashed-pass",
                true, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        when(userRepository.findAuthenticatedUserByEmail("john@test.com")).thenReturn(activeUser);

        UserDetails result = appUserDetailsService.loadUserByUsername("john@test.com");

        assertEquals("john@test.com", result.getUsername());
        assertTrue(result.isEnabled());
        verify(userRepository).findAuthenticatedUserByEmail("john@test.com");
    }

    // Pending user is loaded as disabled.
    @Test
    void returnsDisabledForPendingUser() {
        AuthenticatedUser pendingUser = new AuthenticatedUser(2L, "jane@test.com", "hashed-pass",
                false, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        when(userRepository.findAuthenticatedUserByEmail("jane@test.com")).thenReturn(pendingUser);

        UserDetails result = appUserDetailsService.loadUserByUsername("jane@test.com");

        assertFalse(result.isEnabled());
    }

    // Non-existent email throws UsernameNotFoundException.
    @Test
    void throwsWhenUserNotFound() {
        when(userRepository.findAuthenticatedUserByEmail("nobody@test.com"))
                .thenThrow(new UsernameNotFoundException("User not found: nobody@test.com"));

        assertThrows(UsernameNotFoundException.class,
                () -> appUserDetailsService.loadUserByUsername("nobody@test.com"));
    }
}
