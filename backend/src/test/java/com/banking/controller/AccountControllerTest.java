package com.banking.controller;

import com.banking.dto.AccountResponse;
import com.banking.dto.IbanSearchResponse;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.CustomerNotFoundException;
import com.banking.exception.GlobalExceptionHandler;
import com.banking.security.AuthenticatedUser;
import com.banking.service.interfaces.IAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTest {

    private MockMvc mockMvc;
    private IAccountService accountService;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        accountService = mock(IAccountService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(accountService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(mockAuthUser(), new PageableHandlerMethodArgumentResolver())
                .build();

        accountResponse = new AccountResponse("NL01TEST", "CHECKING", new BigDecimal("1000"),
                BigDecimal.ZERO, new BigDecimal("2000"), true,
                new AccountResponse.UserSummary(1L, "Test", "Customer", "test@test.com"));
    }

    // ── GET /api/accounts/me ────────────────────

    @Test
    void getMyAccountsReturns200WithList() throws Exception {
        when(accountService.findMyAccounts(1L)).thenReturn(List.of(accountResponse));

        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("NL01TEST"))
                .andExpect(jsonPath("$[0].accountType").value("CHECKING"));
    }

    // ── GET /api/accounts ────────────────────

    @Test
    void getAllAccountsReturns200WithPage() throws Exception {
        when(accountService.findAllAccounts(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(accountResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].iban").value("NL01TEST"));
    }

    // ── GET /api/accounts/checking ────────────────────

    @Test
    void searchCheckingReturns200WithMatches() throws Exception {
        when(accountService.searchCustomerCheckingIbansByName("John", "Doe", "", 1L))
                .thenReturn(List.of(new IbanSearchResponse("John", "Doe", "NL01TEST")));

        mockMvc.perform(get("/api/accounts/checking")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].checkingIban").value("NL01TEST"));
    }

    // ── POST /api/accounts ────────────────────

    @Test
    void createAccountReturns201OnSuccess() throws Exception {
        when(accountService.createAccount(any())).thenReturn(accountResponse);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateAccountBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iban").value("NL01TEST"))
                .andExpect(jsonPath("$.accountType").value("CHECKING"));

        verify(accountService).createAccount(any());
    }

    @Test
    void createAccountReturns404WhenCustomerNotFound() throws Exception {
        when(accountService.createAccount(any())).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateAccountBody()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── PUT /api/accounts/{iban} ────────────────────

    @Test
    void updateAccountReturns200OnSuccess() throws Exception {
        when(accountService.updateAccount(any(), any())).thenReturn(accountResponse);

        mockMvc.perform(put("/api/accounts/NL01TEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "active": false }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value("NL01TEST"));

        verify(accountService).updateAccount(any(), any());
    }

    @Test
    void updateAccountReturns404WhenAccountNotFound() throws Exception {
        when(accountService.updateAccount(any(), any())).thenThrow(new AccountNotFoundException("NL99TEST"));

        mockMvc.perform(put("/api/accounts/NL99TEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "active": false }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Helpers ────────────────────

    private String validCreateAccountBody() {
        return """
                {
                  "customerId": 1,
                  "accountType": "CHECKING",
                  "dailyLimit": 2000.00,
                  "absoluteLimit": 0.00
                }
                """;
    }

    private HandlerMethodArgumentResolver mockAuthUser() {
        AuthenticatedUser user = new AuthenticatedUser(1L, "test@test.com", "pass", true,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

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
