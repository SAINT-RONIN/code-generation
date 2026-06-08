package com.banking.controller;

import com.banking.dto.AccountCreateRequest;
import com.banking.dto.AccountResponse;
import com.banking.dto.AccountUpdateRequest;
import com.banking.dto.IbanSearchResponse;
import com.banking.security.AuthenticatedUser;
import com.banking.service.interfaces.IAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Manages bank accounts — customers can view their own accounts and search for
 * transfer recipients; employees can list all accounts, create new ones, and update limits.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account management endpoints")
@SecurityRequirement(name = "bearerAuth") // All endpoints require a valid JWT
public class AccountController {

    private final IAccountService accountService;

    // Constructor injection of the account service
    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Returns the logged-in customer's own active accounts (checking + savings).
     * Used by the frontend to display account cards on the customer dashboard.
     */
    @Operation(summary = "Get my active accounts")
    @ApiResponse(responseCode = "200", description = "Accounts retrieved")
    @GetMapping("/me")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            // @AuthenticationPrincipal injects the currently logged-in user from the SecurityContext
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(accountService.findMyAccounts(caller.getId()));
    }

    /**
     * Returns a paginated list of all customer accounts, with optional filters.
     * Employee-only — used by the employee dashboard to browse and manage accounts.
     */
    @Operation(summary = "Get filtered accounts for employees")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved"),
            @ApiResponse(responseCode = "403", description = "Not an employee")
    })
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')") // Only employees can list all accounts
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @RequestParam(required = false) String ownerEmail,    // Filter by owner's email
            @RequestParam(required = false) Boolean active,       // Filter by active/inactive status
            @RequestParam(required = false) Long userId,          // Filter by specific user ID
            @RequestParam(required = false) String accountType,   // Filter by CHECKING or SAVINGS
            Pageable pageable) {
        return ResponseEntity.ok(accountService.findAllAccounts(ownerEmail, active, userId, accountType, pageable));
    }

    /**
     * Searches for other customers' checking accounts by name or IBAN.
     * Used by the transfer form to find recipients — excludes the caller's own accounts
     * so you can't accidentally send money to yourself via the external transfer flow.
     */
    @Operation(summary = "Search customer checking accounts by name or IBAN")
    @ApiResponse(responseCode = "200", description = "Matching accounts returned")
    @GetMapping("/checking")
    public ResponseEntity<List<IbanSearchResponse>> searchByName(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false, defaultValue = "") String iban, // Optional IBAN search
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(accountService.searchCustomerCheckingIbansByName(firstName, lastName, iban, caller.getId()));
    }

    /**
     * Creates a new bank account (checking or savings) for an existing active customer.
     * Employee-only — used when a customer needs an additional account.
     * Returns 201 Created with the new account details.
     */
    @Operation(summary = "Create a new account for an existing customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Not an employee"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }

    /**
     * Updates an account's active status or limits (daily limit, absolute limit).
     * Employee-only — the account is identified by its IBAN in the URL path.
     */
    @Operation(summary = "Update one account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated"),
            @ApiResponse(responseCode = "403", description = "Not an employee"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/{iban}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable String iban,                              // IBAN from the URL path
            @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(iban, request));
    }
}
