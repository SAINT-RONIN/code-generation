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

    @Test
    void updateAccountReturnsBadRequestWhenAbsoluteLimitIsPositive() throws Exception {
        mockMvc.perform(put("/api/accounts/NL01BANK0123456789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "absoluteLimit": 100.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("absoluteLimit")));

        verifyNoInteractions(accountService);
    }
}
