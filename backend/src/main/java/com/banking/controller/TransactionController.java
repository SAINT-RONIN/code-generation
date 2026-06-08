package com.banking.controller;

import com.banking.dto.TransactionFilter;
import com.banking.dto.TransactionRequest;
import com.banking.dto.TransactionResponse;
import com.banking.security.AuthenticatedUser;
import com.banking.service.interfaces.ITransactionService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles creating and querying transactions (transfers, deposits, withdrawals).
 * Both customers and employees can access these endpoints, but with different rules:
 * - Customers can only operate on their own accounts
 * - Employees can transfer between any two checking accounts of different customers
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction endpoints")
@SecurityRequirement(name = "bearerAuth") // All endpoints require a valid JWT
public class TransactionController {

    private final ITransactionService transactionService;

    // Constructor injection of the transaction service
    public TransactionController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Creates a new transaction. The transaction type (TRANSFER, DEPOSIT, WITHDRAWAL)
     * is automatically inferred from which IBANs are provided:
     * - Both fromIban and toIban → TRANSFER
     * - Only toIban → DEPOSIT
     * - Only fromIban → WITHDRAWAL
     *
     * Employees are routed to a separate flow that enforces checking-to-checking rules.
     */
    @Operation(summary = "Create a transaction (transfer, deposit, or withdrawal)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction completed"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Caller does not own the account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Insufficient funds, daily limit exceeded, or invalid transfer")
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        TransactionResponse response;
        // Route to different service methods based on the caller's role
        if (caller.isEmployee()) {
            response = transactionService.createEmployeeTransfer(request, caller.getEmail());
        } else {
            response = transactionService.createCustomerTransaction(request, caller.getId(), caller.getEmail());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns paginated transaction history with optional filters (date range, amount, IBAN, type).
     * Customers only see transactions involving their own accounts.
     * Employees see all transactions across the entire system.
     */
    @Operation(summary = "Get filtered transaction history")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved")
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            // @ModelAttribute binds query parameters to the TransactionFilter DTO fields
            @ModelAttribute TransactionFilter filter,
            Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        Page<TransactionResponse> page;
        // Employees see all transactions; customers only see their own
        if (caller.isEmployee()) {
            page = transactionService.findAllTransactions(filter, pageable);
        } else {
            page = transactionService.findCustomerTransactions(filter, pageable, caller.getId());
        }
        return ResponseEntity.ok(page);
    }
}
