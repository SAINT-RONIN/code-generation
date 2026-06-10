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

    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASS = "encoded-pass";
    private static final String JWT_TOKEN = "jwt-token";

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private LoginMapper loginMapper;

    @InjectMocks
    private AuthService authService;

    private User activeCustomer;

    // Creates an active customer for login tests.
    @BeforeEach
    void setUp() {
        activeCustomer = new User("John", "Doe", EMAIL, ENCODED_PASS,
                "123456789", "0612345678", User.Role.CUSTOMER);
        activeCustomer.setId(1L);
        activeCustomer.setStatus(UserStatus.ACTIVE);
    }

    // ── Register ────────────────────

    // Registration checks email uniqueness, hashes the password, and saves the user.
    @Test
    void registerSavesNewCustomer() {
        RegisterRequest request = new RegisterRequest("John", "Doe", EMAIL,
                PASSWORD, "123456789", "0612345678");

        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASS);
        when(userRepository.save(any(User.class))).thenReturn(activeCustomer);

        User result = authService.register(request);

        assertEquals(EMAIL, result.getEmail());
        verify(userRepository).ensureEmailAvailable(EMAIL);
        verify(userRepository).save(any(User.class));
    }

    // ── Login ────────────────────

    // Valid credentials return a JWT token and the user's role.
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

    // Wrong password throws and no token is generated.
    @Test
    void loginThrowsWhenPasswordIsWrong() {
        LoginRequest request = new LoginRequest(EMAIL, "wrong-password");

        when(userRepository.findRequiredByEmail(EMAIL)).thenReturn(activeCustomer);
        when(passwordEncoder.matches("wrong-password", ENCODED_PASS)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    // Pending accounts are rejected before password check or token generation.
    @Test
    void loginThrowsWhenAccountIsPending() {
        activeCustomer.setStatus(UserStatus.PENDING);
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        when(userRepository.findRequiredByEmail(EMAIL)).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    // Closed accounts are rejected before password check or token generation.
    @Test
    void loginThrowsWhenAccountIsClosed() {
        activeCustomer.setStatus(UserStatus.CLOSED);
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        when(userRepository.findRequiredByEmail(EMAIL)).thenReturn(activeCustomer);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }
}
