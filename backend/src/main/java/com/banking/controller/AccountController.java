package com.banking.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Get my active accounts")
    @ApiResponse(responseCode = "200", description = "Accounts retrieved")
    @GetMapping("/me")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(accountService.findMyAccounts(caller.getId()));
    }

    @Operation(summary = "Get filtered accounts for employees")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved"),
            @ApiResponse(responseCode = "403", description = "Not an employee")
    })
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @RequestParam(required = false) String ownerEmail,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return ResponseEntity.ok(accountService.findAllAccounts(ownerEmail, active, pageable));
    }

    @Operation(summary = "Search customer checking accounts by name or IBAN")
    @ApiResponse(responseCode = "200", description = "Matching accounts returned")
    @GetMapping("/checking")
    public ResponseEntity<List<IbanSearchResponse>> searchByName(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false, defaultValue = "") String iban,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(accountService.searchCustomerCheckingIbansByName(firstName, lastName, iban, caller.getId()));
    }

    @Operation(summary = "Update one account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated"),
            @ApiResponse(responseCode = "403", description = "Not an employee"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/{iban}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable String iban,
            @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(iban, request));
    }
}
