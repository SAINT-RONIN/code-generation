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
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transfer(request, caller.getUsername()));
    }

    @PostMapping("/atm/deposit")
    public ResponseEntity<TransactionResponse> atmDeposit(@Valid @RequestBody AtmRequest request,
                                                          @AuthenticationPrincipal UserDetails caller) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.atmDeposit(request, caller.getUsername()));
    }

    @PostMapping("/atm/withdraw")
    public ResponseEntity<TransactionResponse> atmWithdraw(@Valid @RequestBody AtmRequest request,
                                                           @AuthenticationPrincipal UserDetails caller) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.atmWithdraw(request, caller.getUsername()));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(@ModelAttribute TransactionFilter filter,
                                                                        Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAllTransactions(filter, pageable));
    }

    @GetMapping("/{iban}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(@PathVariable String iban,
                                                                           @ModelAttribute TransactionFilter filter,
                                                                           Pageable pageable,
                                                                           @AuthenticationPrincipal UserDetails caller) {
        return ResponseEntity.ok(transactionService.findTransactionsForIban(iban, filter, pageable, caller.getUsername()));
    }
}
