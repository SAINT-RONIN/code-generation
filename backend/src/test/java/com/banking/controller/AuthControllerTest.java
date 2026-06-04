package com.banking.controller;

import com.banking.dto.LoginResponse;
import com.banking.exception.EmailAlreadyInUseException;
import com.banking.exception.GlobalExceptionHandler;
import com.banking.security.AuthenticatedUser;
import com.banking.service.interfaces.IAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private IAuthService authService;

    @BeforeEach
    void setUp() {
        authService = mock(IAuthService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(mockAuthUser())
                .build();
    }

    // ── Register ────────────────────

    @Test
    void registerReturns201OnSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterBody()))
                .andExpect(status().isCreated())
                .andExpect(content().string("Registration successful. Await employee approval."));

        verify(authService).register(any());
    }

    @Test
    void registerReturns409WhenEmailAlreadyInUse() throws Exception {
        when(authService.register(any())).thenThrow(new EmailAlreadyInUseException("john@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Login ────────────────────

    @Test
    void loginReturns200WithToken() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("jwt-token", "CUSTOMER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "john@example.com", "password": "secret" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void loginReturns401WhenCredentialsInvalid() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "john@example.com", "password": "wrong" }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Verify PIN ────────────────────

    @Test
    void verifyPinReturns200WhenCorrect() throws Exception {
        mockMvc.perform(post("/api/auth/verify-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "pin": "1234" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PIN verified"));

        verify(authService).verifyPin(1L, "1234");
    }

    @Test
    void verifyPinReturns401WhenIncorrect() throws Exception {
        doThrow(new BadCredentialsException("Incorrect PIN"))
                .when(authService).verifyPin(1L, "0000");

        mockMvc.perform(post("/api/auth/verify-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "pin": "0000" }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Helpers ────────────────────

    private String validRegisterBody() {
        return """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "email": "john@example.com",
                  "password": "secret",
                  "bsn": "123456789",
                  "phoneNumber": "0612345678"
                }
                """;
    }

    private HandlerMethodArgumentResolver mockAuthUser() {
        AuthenticatedUser user = new AuthenticatedUser(1L, "test@test.com", "pass", true,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(AuthenticatedUser.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return user;
            }
        };
    }
}
