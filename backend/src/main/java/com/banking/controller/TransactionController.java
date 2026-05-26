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

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final ITransactionService transactionService;

    public TransactionController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Create a transaction (transfer, deposit, or withdrawal)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction completed"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Caller does not own the account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Insufficient funds, daily limit exceeded, or invalid transfer")
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request,
                                                                  @AuthenticationPrincipal AuthenticatedUser caller) {
        boolean isEmployee = isEmployee(caller);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request, caller.getId(), caller.getEmail(), isEmployee));
    }

    @Operation(summary = "Get filtered transaction history")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved")
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(@ModelAttribute TransactionFilter filter,
                                                                      Pageable pageable,
                                                                      @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(transactionService.findTransactions(filter, pageable, caller.getId(), isEmployee(caller)));
    }

    private boolean isEmployee(AuthenticatedUser caller) {
        return caller.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }
}
