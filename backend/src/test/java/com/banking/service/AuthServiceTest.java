package com.banking.service;

import com.banking.dto.LoginRequest;
import com.banking.dto.LoginResponse;
import com.banking.dto.RegisterRequest;
import com.banking.mapper.LoginMapper;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.UserRepository;
import com.banking.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private LoginMapper loginMapper;

    @InjectMocks
    private AuthService authService;

    private User activeCustomer;

    @BeforeEach
    void setUp() {
        activeCustomer = new User("John", "Doe", "john@example.com", "encoded-pass",
                "123456789", "0612345678", User.Role.CUSTOMER);
        activeCustomer.setId(1L);
        activeCustomer.setStatus(UserStatus.ACTIVE);
    }

    // ── Register ────────────────────

    // Verifies that registration checks email uniqueness, hashes the password, and persists the user
    @Test
    void registerSavesNewCustomer() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com",
                "password", "123456789", "0612345678");

        when(passwordEncoder.encode("password")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenReturn(activeCustomer);

        User result = authService.register(request);

        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).ensureEmailAvailable("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    // ── Login ────────────────────

    // Happy path: valid credentials on an active account should return a JWT token and the user's role
    @Test
    void loginReturnsTokenForActiveUser() {
        LoginRequest request = new LoginRequest("john@example.com", "password");
        LoginResponse response = new LoginResponse("jwt-token", "CUSTOMER");

        when(userRepository.findRequiredByEmail("john@example.com")).thenReturn(activeCustomer);
        when(passwordEncoder.matches("password", "encoded-pass")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "john@example.com")).thenReturn("jwt-token");
        when(loginMapper.toResponse("jwt-token", activeCustomer)).thenReturn(response);

        LoginResponse result = authService.login(request);

        assertEquals("jwt-token", result.token());
        assertEquals("CUSTOMER", result.role());
    }

    // Wrong password must reject login and never generate a token
    @Test
    void loginThrowsWhenPasswordIsWrong() {
        LoginRequest request = new LoginRequest("john@example.com", "wrong-password");

        when(userRepository.findRequiredByEmail("john@example.com")).thenReturn(activeCustomer);
        when(passwordEncoder.matches("wrong-password", "encoded-pass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    // Pending accounts must not log in — the flow should stop before password check or token generation
    @Test
    void loginThrowsWhenAccountIsPending() {
        activeCustomer.setStatus(UserStatus.PENDING);
        LoginRequest request = new LoginRequest("john@example.com", "password");

        when(userRepository.findRequiredByEmail("john@example.com")).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    // Closed accounts must not log in — the flow should stop before password check or token generation
    @Test
    void loginThrowsWhenAccountIsClosed() {
        activeCustomer.setStatus(UserStatus.CLOSED);
        LoginRequest request = new LoginRequest("john@example.com", "password");

        when(userRepository.findRequiredByEmail("john@example.com")).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    // ── Verify PIN ────────────────────

    // Correct PIN should pass without throwing — used before ATM operations
    @Test
    void verifyPinPassesWhenPinMatches() {
        activeCustomer.setPin("encoded-pin");

        when(userRepository.findRequiredById(1L)).thenReturn(activeCustomer);
        when(passwordEncoder.matches("1234", "encoded-pin")).thenReturn(true);

        authService.verifyPin(1L, "1234");

        verify(userRepository).findRequiredById(1L);
    }

    // Wrong PIN must be rejected to prevent unauthorized ATM access
    @Test
    void verifyPinThrowsWhenPinIsWrong() {
        activeCustomer.setPin("encoded-pin");

        when(userRepository.findRequiredById(1L)).thenReturn(activeCustomer);
        when(passwordEncoder.matches("0000", "encoded-pin")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.verifyPin(1L, "0000"));
    }

    // A user who never set a PIN should be rejected rather than crashing on a null comparison
    @Test
    void verifyPinThrowsWhenPinIsNull() {
        activeCustomer.setPin(null);

        when(userRepository.findRequiredById(1L)).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.verifyPin(1L, "1234"));
    }
}
