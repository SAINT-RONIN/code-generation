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

    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASS = "encoded-pass";
    private static final String JWT_TOKEN = "jwt-token";
    private static final String PIN = "1234";
    private static final String ENCODED_PIN = "encoded-pin";

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
        activeCustomer = new User("John", "Doe", EMAIL, ENCODED_PASS,
                "123456789", "0612345678", User.Role.CUSTOMER);
        activeCustomer.setId(1L);
        activeCustomer.setStatus(UserStatus.ACTIVE);
    }

    // ── Register ────────────────────

    /** Verifies that registration checks email uniqueness, hashes the password, and persists the user */
    @Test
    void registerSavesNewCustomer() {
        RegisterRequest request = new RegisterRequest("John", "Doe", EMAIL,
                PASSWORD, "123456789", "0612345678");

        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASS);
        when(userRepository.save(any(User.class))).thenReturn(activeCustomer);

        User result = authService.register(request);

        assertEquals(EMAIL, result.getEmail());
        // Verify email uniqueness was checked before saving
        verify(userRepository).ensureEmailAvailable(EMAIL);
        verify(userRepository).save(any(User.class));
    }

    // ── Login ────────────────────

    
    /** Happy path: valid credentials on an active account should return a JWT token and the user's role */
    @Test
    void loginReturnsTokenForActiveUser() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        LoginResponse response = new LoginResponse(JWT_TOKEN, "CUSTOMER");

        when(userRepository.findRequiredByEmail(EMAIL)).thenReturn(activeCustomer);
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASS)).thenReturn(true);
        when(jwtUtil.generateToken(1L, EMAIL)).thenReturn(JWT_TOKEN);
        when(loginMapper.toResponse(JWT_TOKEN, activeCustomer)).thenReturn(response);

        LoginResponse result = authService.login(request);

        assertEquals(JWT_TOKEN, result.token());
        assertEquals("CUSTOMER", result.role());
    }

    /** Wrong password must reject login and never generate a token */
    @Test
    void loginThrowsWhenPasswordIsWrong() {
        LoginRequest request = new LoginRequest(EMAIL, "wrong-password");

        when(userRepository.findRequiredByEmail(EMAIL)).thenReturn(activeCustomer);
        when(passwordEncoder.matches("wrong-password", ENCODED_PASS)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        // Token should never be generated when password is wrong
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    /** Pending accounts must not log in — the flow should stop before password check or token generation */
    @Test
    void loginThrowsWhenAccountIsPending() {
        activeCustomer.setStatus(UserStatus.PENDING);
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        when(userRepository.findRequiredByEmail(EMAIL)).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        // Neither password check nor token generation should happen for pending accounts because they haven't been created
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    /** Closed accounts must not log in — the flow should stop before password check or token generation */
    @Test
    void loginThrowsWhenAccountIsClosed() {
        activeCustomer.setStatus(UserStatus.CLOSED);
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        when(userRepository.findRequiredByEmail(EMAIL)).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        // Same as pending — short-circuit before expensive operations
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    // ── Verify PIN ────────────────────

    /** Correct PIN should pass without throwing — used before ATM operations */
    @Test
    void verifyPinPassesWhenPinMatches() {
        activeCustomer.setPin(ENCODED_PIN);

        when(userRepository.findRequiredById(1L)).thenReturn(activeCustomer);
        when(passwordEncoder.matches(PIN, ENCODED_PIN)).thenReturn(true);

        // Should complete without exception
        authService.verifyPin(1L, PIN);

        verify(userRepository).findRequiredById(1L);
    }

    /** Wrong PIN must be rejected to prevent unauthorized ATM access */
    @Test
    void verifyPinThrowsWhenPinIsWrong() {
        activeCustomer.setPin(ENCODED_PIN);

        when(userRepository.findRequiredById(1L)).thenReturn(activeCustomer);
        when(passwordEncoder.matches("0000", ENCODED_PIN)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.verifyPin(1L, "0000"));
    }

    /** A user who never set a PIN should be rejected rather than crashing on a null comparison */
    @Test
    void verifyPinThrowsWhenPinIsNull() {
        // PIN is null — the service must handle this gracefully (null check before matches())
        activeCustomer.setPin(null);

        when(userRepository.findRequiredById(1L)).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.verifyPin(1L, PIN));
    }
}
