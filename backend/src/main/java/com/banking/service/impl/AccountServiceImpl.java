package com.banking.service.impl;

import com.banking.dto.AccountResponse;
import com.banking.dto.IbanSearchResponse;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.service.interfaces.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AccountServiceImpl(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<IbanSearchResponse> searchCustomerCheckingIbansByName(String firstName, String lastName) {
        return userRepository.searchApprovedCustomersByName(firstName, lastName)
                .stream()
                .filter(customer -> customer.getAccounts().stream().anyMatch(a -> a.getAccountType() == AccountType.CHECKING))
                .map(this::mapUserToIbanSearchResponse)
                .toList();
    }

    @Override
    public List<AccountResponse> findMyAccounts(String email) {
        return accountRepository.findAllByUserEmailAndActiveTrue(email).stream()
                .map(AccountResponse::from)
                .toList();
    }

    private IbanSearchResponse mapUserToIbanSearchResponse(User customer) {
        String checkingIban = customer.getAccounts().stream()
                .filter(account -> account.getAccountType() == AccountType.CHECKING)
                .map(com.banking.model.Account::getIban)
                .findFirst()
                .orElseThrow();
        return new IbanSearchResponse(customer.getFirstName(), customer.getLastName(), checkingIban);
    }
}
