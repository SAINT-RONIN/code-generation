package com.banking.controller;

import com.banking.dto.CustomerResponse;
import com.banking.exception.CustomerNotFoundException;
import com.banking.exception.GlobalExceptionHandler;
import com.banking.security.AuthenticatedUser;
import com.banking.service.interfaces.ICustomerService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerControllerTest {

    private MockMvc mockMvc;
    private ICustomerService customerService;
    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {
        customerService = mock(ICustomerService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new CustomerController(customerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(mockAuthUser(), new PageableHandlerMethodArgumentResolver())
                .build();

        customerResponse = new CustomerResponse(2L, "John", "Doe",
                "john@example.com", "123456789", "0612345678", "ACTIVE");
    }

    // ── GET /api/customers ────────────────────

    @Test
    void getCustomersReturns200WithPage() throws Exception {
        when(customerService.findCustomers(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(customerResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"));
    }

    // ── PUT /api/customers/{id} ────────────────────

    @Test
    void updateCustomerReturns200OnSuccess() throws Exception {
        when(customerService.updateCustomer(any(), any())).thenReturn(customerResponse);

        mockMvc.perform(put("/api/customers/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "ACTIVE", "dailyLimit": 3000.00, "absoluteLimit": -100.00 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(customerService).updateCustomer(any(), any());
    }

    @Test
    void updateCustomerReturns404WhenNotFound() throws Exception {
        when(customerService.updateCustomer(any(), any())).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(put("/api/customers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "ACTIVE" }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Helpers ────────────────────

    private HandlerMethodArgumentResolver mockAuthUser() {
        AuthenticatedUser user = new AuthenticatedUser(1L, "employee@bank.com", "pass", true,
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
