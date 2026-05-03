package com.banking.controller;

import com.banking.dto.*;
import com.banking.service.interfaces.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
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
}
