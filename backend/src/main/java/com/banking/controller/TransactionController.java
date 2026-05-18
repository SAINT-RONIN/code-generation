package com.banking.controller;

import com.banking.dto.*;
import com.banking.security.AuthenticatedUser;
import com.banking.service.interfaces.ITransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transfer, cash, and transaction history endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final ITransactionService transactionService;

    public TransactionController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Transfer money between accounts")
    @PostMapping
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request,
                                                        @AuthenticationPrincipal AuthenticatedUser caller) {
        boolean isEmployee = isEmployee(caller);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.transfer(request, caller.getId(), caller.getEmail(), isEmployee));
    }

    @Operation(summary = "Deposit cash into one account")
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody AtmRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.deposit(request, caller.getId(), caller.getEmail()));
    }

    @Operation(summary = "Withdraw cash from one account")
    @PostMapping("/withdrawal")
    public ResponseEntity<TransactionResponse> withdrawal(@Valid @RequestBody AtmRequest request,
                                                          @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.withdrawal(request, caller.getId(), caller.getEmail()));
    }

    @Operation(summary = "Get filtered transaction history")
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
