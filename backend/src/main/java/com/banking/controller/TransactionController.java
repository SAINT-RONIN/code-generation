package com.banking.controller;

import com.banking.dto.*;
import com.banking.service.interfaces.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request,
                                                        @AuthenticationPrincipal UserDetails caller) {
        boolean isEmployee = isEmployee(caller);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.transfer(request, caller.getUsername(), isEmployee));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody AtmRequest request,
                                                       @AuthenticationPrincipal UserDetails caller) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.deposit(request, caller.getUsername()));
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<TransactionResponse> withdrawal(@Valid @RequestBody AtmRequest request,
                                                          @AuthenticationPrincipal UserDetails caller) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.withdrawal(request, caller.getUsername()));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(@ModelAttribute TransactionFilter filter,
                                                                      Pageable pageable,
                                                                      @AuthenticationPrincipal UserDetails caller) {
        return ResponseEntity.ok(transactionService.findTransactions(filter, pageable, caller.getUsername(), isEmployee(caller)));
    }

    private boolean isEmployee(UserDetails caller) {
        return caller.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }
}
