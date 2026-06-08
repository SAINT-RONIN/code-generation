package com.banking.controller;

import com.banking.dto.CustomerResponse;
import com.banking.dto.CustomerUpdateRequest;
import com.banking.model.User.UserStatus;
import com.banking.service.interfaces.ICustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Employee-only endpoints for viewing and managing customer accounts.
 * All endpoints require the EMPLOYEE role — enforced via @PreAuthorize.
 */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
@SecurityRequirement(name = "bearerAuth") // All endpoints here require a JWT token
public class CustomerController {

    private final ICustomerService customerService;

    // Constructor injection of the customer service
    public CustomerController(ICustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Returns a paginated list of customers, optionally filtered by status and search text.
     * Used by the employee dashboard to list pending, active, or closed customers.
     * The Pageable parameter is auto-populated from query params like ?page=0&size=10&sort=firstName,asc
     */
    @Operation(summary = "Get filtered customers for employees")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customers retrieved"),
            @ApiResponse(responseCode = "403", description = "Not an employee")
    })
    @GetMapping
    // @PreAuthorize checks the caller's role BEFORE the method runs — returns 403 if not an employee
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Page<CustomerResponse>> getCustomers(
            @RequestParam(required = false) String status,   // Optional: "PENDING", "ACTIVE", or "CLOSED"
            @RequestParam(required = false) String search,   // Optional: search by name or email
            Pageable pageable) {                             // Spring auto-binds ?page=&size=&sort= params
        // Convert the string status to the UserStatus enum (null if not provided)
        UserStatus userStatus = status != null ? UserStatus.valueOf(status) : null;
        return ResponseEntity.ok(customerService.findCustomers(userStatus, search, pageable));
    }

    /**
     * Updates a customer's status (approve, close, reactivate) or their account limits.
     * Used by employees to approve pending registrations or adjust daily/absolute limits.
     */
    @Operation(summary = "Update one customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer updated"),
            @ApiResponse(responseCode = "403", description = "Not an employee"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,                                  // Customer ID from the URL path
            @Valid @RequestBody CustomerUpdateRequest request) {     // Request body with status/limits
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }
}
