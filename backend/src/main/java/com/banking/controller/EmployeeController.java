package com.banking.controller;

import com.banking.dto.*;
import com.banking.service.interfaces.EmployeeService;
import com.banking.service.interfaces.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final TransactionService transactionService;

    public EmployeeController(EmployeeService employeeService, TransactionService transactionService) {
        this.employeeService = employeeService;
        this.transactionService = transactionService;
    }

    @GetMapping("/customers/pending")
    public ResponseEntity<List<PendingCustomerResponse>> getPendingCustomers() {
        return ResponseEntity.ok(employeeService.findPendingCustomers());
    }

    @PostMapping("/customers/{id}/approve")
    public ResponseEntity<Void> approveCustomer(@PathVariable Long id,
                                                @Valid @RequestBody ApproveCustomerRequest request) {
        employeeService.approveCustomerAndCreateAccounts(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/accounts")
    public ResponseEntity<Page<AccountResponse>> getAllCustomerAccounts(Pageable pageable) {
        return ResponseEntity.ok(employeeService.findAllCustomerAccounts(pageable));
    }

    @PutMapping("/accounts/{iban}/limits")
    public ResponseEntity<Void> updateAccountLimits(@PathVariable String iban,
                                                    @Valid @RequestBody UpdateLimitsRequest request) {
        employeeService.updateAccountLimits(iban, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/accounts/{iban}")
    public ResponseEntity<Void> closeAccount(@PathVariable String iban) {
        employeeService.closeAccount(iban);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> transferBetweenCustomers(@Valid @RequestBody TransferRequest request,
                                                                         @AuthenticationPrincipal UserDetails employee) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.employeeTransfer(request, employee.getUsername()));
    }
}
