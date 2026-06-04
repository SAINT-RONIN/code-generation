package com.banking.controller;

import com.banking.dto.TransactionResponse;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.GlobalExceptionHandler;
import com.banking.exception.InsufficientFundsException;
import com.banking.exception.UnauthorizedAccountAccessException;
import com.banking.security.AuthenticatedUser;
import com.banking.service.interfaces.ITransactionService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerTest {

    private MockMvc mockMvc;
    private ITransactionService transactionService;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        transactionService = mock(ITransactionService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(transactionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(mockAuthUser(), new PageableHandlerMethodArgumentResolver())
                .build();

        transactionResponse = new TransactionResponse(1L, "NL01TEST", "NL02TEST",
                new BigDecimal("200.00"), LocalDateTime.now(), "test@test.com", "Payment", "TRANSFER");
    }

    // ── POST /api/transactions ────────────────────

    @Test
    void createTransactionReturns201OnSuccess() throws Exception {
        when(transactionService.createTransaction(any(), anyLong(), anyString(), anyBoolean()))
                .thenReturn(transactionResponse);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTransferBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromIban").value("NL01TEST"))
                .andExpect(jsonPath("$.toIban").value("NL02TEST"))
                .andExpect(jsonPath("$.transactionType").value("TRANSFER"));

        verify(transactionService).createTransaction(any(), anyLong(), anyString(), anyBoolean());
    }

    @Test
    void createTransactionReturns422WhenInsufficientFunds() throws Exception {
        when(transactionService.createTransaction(any(), anyLong(), anyString(), anyBoolean()))
                .thenThrow(new InsufficientFundsException("NL01TEST"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTransferBody()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createTransactionReturns403WhenUnauthorized() throws Exception {
        when(transactionService.createTransaction(any(), anyLong(), anyString(), anyBoolean()))
                .thenThrow(new UnauthorizedAccountAccessException("Not your account"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTransferBody()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createTransactionReturns404WhenAccountNotFound() throws Exception {
        when(transactionService.createTransaction(any(), anyLong(), anyString(), anyBoolean()))
                .thenThrow(new AccountNotFoundException("NL99TEST"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTransferBody()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── GET /api/transactions ────────────────────

    @Test
    void getTransactionsReturns200WithPage() throws Exception {
        when(transactionService.findTransactions(any(), any(), anyLong(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of(transactionResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fromIban").value("NL01TEST"))
                .andExpect(jsonPath("$.content[0].transactionType").value("TRANSFER"));
    }

    // ── Helpers ────────────────────

    private String validTransferBody() {
        return """
                {
                  "fromIban": "NL01TEST",
                  "toIban": "NL02TEST",
                  "amount": 200.00,
                  "description": "Payment"
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
