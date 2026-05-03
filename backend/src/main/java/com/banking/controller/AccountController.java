package com.banking.controller;

import com.banking.dto.IbanSearchResponse;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final UserRepository userRepository;

    public AccountController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<List<IbanSearchResponse>> searchByName(@RequestParam String firstName,
                                                                  @RequestParam String lastName) {
        List<IbanSearchResponse> results = userRepository.searchApprovedCustomersByName(firstName, lastName)
                .stream()
                .map(this::toIbanSearchResponse)
                .toList();
        return ResponseEntity.ok(results);
    }

    private IbanSearchResponse toIbanSearchResponse(User user) {
        String checkingIban = user.getAccounts().stream()
                .filter(a -> a.getAccountType() == AccountType.CHECKING)
                .map(Account::getIban)
                .findFirst()
                .orElse(null);
        return new IbanSearchResponse(user.getFirstName(), user.getLastName(), checkingIban);
    }
}
