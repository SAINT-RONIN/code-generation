package com.banking.controller;

import com.banking.dto.AccountResponse;
import com.banking.dto.IbanSearchResponse;
import com.banking.service.interfaces.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(@AuthenticationPrincipal UserDetails caller) {
        return ResponseEntity.ok(accountService.findMyAccounts(caller.getUsername()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<IbanSearchResponse>> searchByName(@RequestParam String firstName, @RequestParam String lastName) {
        return ResponseEntity.ok(accountService.searchCustomerCheckingIbansByName(firstName, lastName));
    }
}
