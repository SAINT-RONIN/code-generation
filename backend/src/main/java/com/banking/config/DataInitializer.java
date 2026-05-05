package com.banking.config;

import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.employee.email}")
    private String employeeEmail;

    @Value("${seed.employee.password}")
    private String employeePassword;

    @Value("${seed.customer.email}")
    private String customerEmail;

    @Value("${seed.customer.password}")
    private String customerPassword;

    public DataInitializer(UserRepository userRepository, AccountRepository accountRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(employeeEmail)) seedEmployee();
        if (!userRepository.existsByEmail(customerEmail)) seedTestCustomer();
    }

    private void seedEmployee() {
        User employee = new User(
                "Bank", "Employee",
                employeeEmail,
                passwordEncoder.encode(employeePassword),
                "000000000", "0600000000",
                User.Role.EMPLOYEE
        );
        employee.setStatus(UserStatus.ACTIVE);
        employee.setPin(passwordEncoder.encode("1234"));
        userRepository.save(employee);
    }

    private void seedTestCustomer() {
        User customer = new User(
                "Test", "Customer",
                customerEmail,
                passwordEncoder.encode(customerPassword),
                "123456789", "0612345678",
                User.Role.CUSTOMER
        );
        customer.setStatus(UserStatus.ACTIVE);
        customer.setPin(passwordEncoder.encode("1234"));
        customer = userRepository.save(customer);
        accountRepository.save(buildAccount(generateUniqueIban(), AccountType.CHECKING, new BigDecimal("2000.00"), customer));
        accountRepository.save(buildAccount(generateUniqueIban(), AccountType.SAVINGS, new BigDecimal("500.00"), customer));
    }

    private Account buildAccount(String iban, AccountType type, BigDecimal dailyLimit, User customer) {
        Account account = new Account(iban, type, BigDecimal.ZERO, dailyLimit, customer);
        account.setBalance(new BigDecimal("1500.00"));
        return account;
    }

    private String generateUniqueIban() {
        String iban;
        do {
            iban = Iban.random(CountryCode.NL).toString();
        } while (accountRepository.existsById(iban));
        return iban;
    }
}
