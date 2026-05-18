package com.banking.controller;

import com.banking.dto.CustomerResponse;
import com.banking.dto.CustomerUpdateRequest;
import com.banking.model.User.UserStatus;
import com.banking.service.interfaces.ICustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final ICustomerService customerService;

    public CustomerController(ICustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Get filtered customers for employees")
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Page<CustomerResponse>> getCustomers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        UserStatus userStatus = status != null ? UserStatus.valueOf(status) : null;
        return ResponseEntity.ok(customerService.findCustomers(userStatus, search, pageable));
    }

    @Operation(summary = "Update one customer")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }
}
