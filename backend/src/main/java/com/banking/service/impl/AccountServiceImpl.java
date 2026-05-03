package com.banking.service.impl;

import com.banking.dto.IbanSearchResponse;
import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.repository.UserRepository;
import com.banking.service.interfaces.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;

    public AccountServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<IbanSearchResponse> searchCustomerCheckingIbansByName(String firstName, String lastName) {
        return userRepository.searchApprovedCustomersByName(firstName, lastName)
                .stream()
                .map(this::mapUserToIbanSearchResponse)
                .toList();
    }

    private IbanSearchResponse mapUserToIbanSearchResponse(User customer) {
        String checkingIban = customer.getAccounts().stream()
                .filter(account -> account.getAccountType() == AccountType.CHECKING)
                .map(Account::getIban)
                .findFirst()
                .orElse(null);
        return new IbanSearchResponse(customer.getFirstName(), customer.getLastName(), checkingIban);
    }
}
