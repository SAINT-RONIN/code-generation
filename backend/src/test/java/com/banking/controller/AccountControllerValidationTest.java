package com.banking.controller;

import com.banking.exception.GlobalExceptionHandler;
import com.banking.service.interfaces.IAccountService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerValidationTest {

    private MockMvc mockMvc;
    private IAccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = mock(IAccountService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(accountService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    // ── Update account validation ────────────────────

    @Test
    void updateAccountRejectsPositiveAbsoluteLimit() throws Exception {
        mockMvc.perform(put("/api/accounts/NL01BANK0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "absoluteLimit": 100.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("absoluteLimit")));

        verifyNoInteractions(accountService);
    }

    @Test
    void updateAccountRejectsNegativeDailyLimit() throws Exception {
        mockMvc.perform(put("/api/accounts/NL01BANK0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "dailyLimit": -100.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("dailyLimit")));

        verifyNoInteractions(accountService);
    }

    // ── Create account validation ────────────────────

    @Test
    void createAccountRejectsMissingCustomerId() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountType": "CHECKING",
                                  "dailyLimit": 2000.00,
                                  "absoluteLimit": 0.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("customerId")));

        verifyNoInteractions(accountService);
    }

    @Test
    void createAccountRejectsInvalidAccountType() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1,
                                  "accountType": "INVALID",
                                  "dailyLimit": 2000.00,
                                  "absoluteLimit": 0.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("accountType")));

        verifyNoInteractions(accountService);
    }

    @Test
    void createAccountRejectsNegativeDailyLimit() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1,
                                  "accountType": "CHECKING",
                                  "dailyLimit": -500.00,
                                  "absoluteLimit": 0.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("dailyLimit")));

        verifyNoInteractions(accountService);
    }

    @Test
    void createAccountRejectsPositiveAbsoluteLimit() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1,
                                  "accountType": "CHECKING",
                                  "dailyLimit": 2000.00,
                                  "absoluteLimit": 100.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("absoluteLimit")));

        verifyNoInteractions(accountService);
    }
}
