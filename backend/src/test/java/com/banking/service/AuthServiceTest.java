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

/**
 * Unit tests for AuthService using Mockito mocks.
 * Tests registration, login, and PIN verification logic in isolation
 * without a database or Spring context.
 *
 * Uses @Mock for dependencies and @InjectMocks to wire them into the service.
 */
@ExtendWith(MockitoExtension.class) // Enables Mockito annotations (@Mock, @InjectMocks) without Spring
class AuthServiceTest {

    @Mock private UserRepository userRepository;     // Mocked — no real database calls
    @Mock private PasswordEncoder passwordEncoder;   // Mocked — no real BCrypt hashing
    @Mock private JwtUtil jwtUtil;                   // Mocked — no real JWT generation
    @Mock private LoginMapper loginMapper;           // Mocked — no real DTO mapping

    @InjectMocks // Creates a real AuthService and injects the mocks above into its constructor
    private AuthService authService;

    private User activeCustomer;

    /**
     * Creates a reusable active customer entity for login and PIN tests.
     * The ID is set manually because there's no database to auto-generate it.
     */
    @BeforeEach
    void setUp() {
        activeCustomer = new User("John", "Doe", "john@example.com", "encoded-pass",
                "123456789", "0612345678", User.Role.CUSTOMER);
        activeCustomer.setId(1L);
        activeCustomer.setStatus(UserStatus.ACTIVE);
    }

    // ── Register ────────────────────

    /** Verifies that registration checks email uniqueness, hashes the password, and persists the user */
    @Test
    void registerSavesNewCustomer() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com",
                "password", "123456789", "0612345678");

        when(passwordEncoder.encode("password")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenReturn(activeCustomer);

        User result = authService.register(request);

        assertEquals("john@example.com", result.getEmail());
        // Verify email uniqueness was checked before saving
        verify(userRepository).ensureEmailAvailable("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    // ── Login ────────────────────

    
    /** Happy path: valid credentials on an active account should return a JWT token and the user's role */
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

    /** Wrong password must reject login and never generate a token */
    @Test
    void loginThrowsWhenPasswordIsWrong() {
        LoginRequest request = new LoginRequest("john@example.com", "wrong-password");

        when(userRepository.findRequiredByEmail("john@example.com")).thenReturn(activeCustomer);
        when(passwordEncoder.matches("wrong-password", "encoded-pass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        // Token should never be generated when password is wrong
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    /** Pending accounts must not log in — the flow should stop before password check or token generation */
    @Test
    void loginThrowsWhenAccountIsPending() {
        activeCustomer.setStatus(UserStatus.PENDING);
        LoginRequest request = new LoginRequest("john@example.com", "password");

        when(userRepository.findRequiredByEmail("john@example.com")).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        // Neither password check nor token generation should happen for pending accounts
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    /** Closed accounts must not log in — the flow should stop before password check or token generation */
    @Test
    void loginThrowsWhenAccountIsClosed() {
        activeCustomer.setStatus(UserStatus.CLOSED);
        LoginRequest request = new LoginRequest("john@example.com", "password");

        when(userRepository.findRequiredByEmail("john@example.com")).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        // Same as pending — short-circuit before expensive operations
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    // ── Verify PIN ────────────────────

    /** Correct PIN should pass without throwing — used before ATM operations */
    @Test
    void verifyPinPassesWhenPinMatches() {
        activeCustomer.setPin("encoded-pin");

        when(userRepository.findRequiredById(1L)).thenReturn(activeCustomer);
        when(passwordEncoder.matches("1234", "encoded-pin")).thenReturn(true);

        // Should complete without exception
        authService.verifyPin(1L, "1234");

        verify(userRepository).findRequiredById(1L);
    }

    /** Wrong PIN must be rejected to prevent unauthorized ATM access */
    @Test
    void verifyPinThrowsWhenPinIsWrong() {
        activeCustomer.setPin("encoded-pin");

        when(userRepository.findRequiredById(1L)).thenReturn(activeCustomer);
        when(passwordEncoder.matches("0000", "encoded-pin")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.verifyPin(1L, "0000"));
    }

    /** A user who never set a PIN should be rejected rather than crashing on a null comparison */
    @Test
    void verifyPinThrowsWhenPinIsNull() {
        // PIN is null — the service must handle this gracefully (null check before matches())
        activeCustomer.setPin(null);

        when(userRepository.findRequiredById(1L)).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.verifyPin(1L, "1234"));
    }
}
