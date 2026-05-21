package com.banking.controller;

import com.banking.dto.AccountResponse;
import com.banking.dto.AccountUpdateRequest;
import com.banking.dto.IbanSearchResponse;
import com.banking.security.AuthenticatedUser;
import com.banking.service.interfaces.IAccountService;
import io.swagger.v3.oas.annotations.Operation;
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

    // Return the signed-in customer's own active accounts.
    @Operation(summary = "Get my active accounts")
    @GetMapping("/me")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(accountService.findMyAccounts(caller.getId()));
    }

    // Return a filtered page of accounts for employee management screens.
    @Operation(summary = "Get filtered accounts for employees")
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @RequestParam(required = false) String ownerEmail,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return ResponseEntity.ok(accountService.findAllAccounts(ownerEmail, active, pageable));
    }

    // Search customer checking accounts by name or IBAN, excluding the caller's own accounts.
    @Operation(summary = "Search customer checking accounts by name or IBAN")
    @GetMapping("/search")
    public ResponseEntity<List<IbanSearchResponse>> searchByName(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false, defaultValue = "") String iban,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(accountService.searchCustomerCheckingIbansByName(firstName, lastName, iban, caller.getId()));
    }

    // Update the selected fields of one account.
    @Operation(summary = "Update one account")
    @PutMapping("/{iban}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable String iban,
            @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(iban, request));
    }
}
