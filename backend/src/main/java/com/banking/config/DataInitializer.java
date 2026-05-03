package com.banking.config;

import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.util.IbanGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, AccountRepository accountRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("employee@bank.com")) seedEmployee();
        if (!userRepository.existsByEmail("customer@test.com")) seedTestCustomer();
    }

    private void seedEmployee() {
        userRepository.save(new User(
                "Bank", "Employee",
                "employee@bank.com",
                passwordEncoder.encode("employee123"),
                "000000000", "0600000000",
                User.Role.EMPLOYEE
        ));
    }

    private void seedTestCustomer() {
        User customer = userRepository.save(new User(
                "Test", "Customer",
                "customer@test.com",
                passwordEncoder.encode("customer123"),
                "123456789", "0612345678",
                User.Role.CUSTOMER
        ));
        accountRepository.save(new Account(
                generateUniqueIban(), AccountType.CHECKING,
                BigDecimal.ZERO, new BigDecimal("2000.00"), customer
        ));
        accountRepository.save(new Account(
                generateUniqueIban(), AccountType.SAVINGS,
                BigDecimal.ZERO, new BigDecimal("500.00"), customer
        ));
        setStartingBalance(customer);
    }

    private void setStartingBalance(User customer) {
        accountRepository.findAllByUserEmail(customer.getEmail()).forEach(account -> {
            account.setBalance(new BigDecimal("1500.00"));
            accountRepository.save(account);
        });
    }

    private String generateUniqueIban() {
        String iban;
        do { iban = IbanGenerator.generate(); } while (accountRepository.existsByIban(iban));
        return iban;
    }
}
