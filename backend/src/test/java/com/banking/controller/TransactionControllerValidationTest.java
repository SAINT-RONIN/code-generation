package com.banking.controller;

import com.banking.exception.GlobalExceptionHandler;
import com.banking.service.interfaces.ITransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerValidationTest {

    private MockMvc mockMvc;
    private ITransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = mock(ITransactionService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(transactionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    // ── Create transaction validation ────────────────────

    @Test
    void createTransactionRejectsMissingAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01BANK0123456789",
                                  "toIban": "NL02BANK0123456789",
                                  "type": "TRANSFER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("amount")));

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransactionRejectsZeroAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01BANK0123456789",
                                  "toIban": "NL02BANK0123456789",
                                  "amount": 0.00,
                                  "type": "TRANSFER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("amount")));

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransactionRejectsNegativeAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01BANK0123456789",
                                  "toIban": "NL02BANK0123456789",
                                  "amount": -50.00,
                                  "type": "TRANSFER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("amount")));

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransactionRejectsMissingType() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "NL01BANK0123456789",
                                  "toIban": "NL02BANK0123456789",
                                  "amount": 100.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("type")));

        verifyNoInteractions(transactionService);
    }
}
