package com.banking.config;

import com.banking.model.Account;
import com.banking.model.Account.AccountType;
import com.banking.model.Transaction;
import com.banking.model.Transaction.TransactionType;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.employee.email}")
    private String employeeEmail;

    @Value("${seed.employee.password}")
    private String employeePassword;

    @Value("${seed.customer.email}")
    private String customerEmail;

    @Value("${seed.customer.password}")
    private String customerPassword;

    @Value("${seed.customer2.email}")
    private String customer2Email;

    @Value("${seed.customer2.password}")
    private String customer2Password;

    public DataInitializer(UserRepository userRepository, AccountRepository accountRepository,
                           TransactionRepository transactionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(employeeEmail)) return;

        seedEmployee();
        List<Account> customer1Accounts = seedTestCustomer();
        List<Account> customer2Accounts = seedTestCustomer2();
        seedTransactionHistory(customer1Accounts, customer2Accounts);
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

    private List<Account> seedTestCustomer() {
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
        return accountRepository.saveAll(List.of(
                new Account(generateUniqueIban(), AccountType.CHECKING, new BigDecimal("1500.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer),
                new Account(generateUniqueIban(), AccountType.SAVINGS, new BigDecimal("1500.00"), BigDecimal.ZERO, new BigDecimal("500.00"), customer)
        ));
    }

    private List<Account> seedTestCustomer2() {
        User customer = new User(
                "Test", "Customer2",
                customer2Email,
                passwordEncoder.encode(customer2Password),
                "987654321", "0687654321",
                User.Role.CUSTOMER
        );
        customer.setStatus(UserStatus.ACTIVE);
        customer.setPin(passwordEncoder.encode("1234"));
        customer = userRepository.save(customer);
        return accountRepository.saveAll(List.of(
                new Account(generateUniqueIban(), AccountType.CHECKING, new BigDecimal("1500.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer),
                new Account(generateUniqueIban(), AccountType.SAVINGS, new BigDecimal("1500.00"), BigDecimal.ZERO, new BigDecimal("500.00"), customer)
        ));
    }

    private void seedTransactionHistory(List<Account> c1Accounts, List<Account> c2Accounts) {
        Account c1Checking = c1Accounts.get(0);
        Account c1Savings  = c1Accounts.get(1);
        Account c2Checking = c2Accounts.get(0);
        Account c2Savings  = c2Accounts.get(1);

        LocalDateTime now = LocalDateTime.now();

        // Customer 1 deposits cash at ATM (7 days ago)
        seedTransaction(null, c1Checking.getIban(), "500.00", customerEmail, "ATM cash deposit", TransactionType.DEPOSIT, now.minusDays(7));
        c1Checking.credit(new BigDecimal("500.00"));

        // Customer 2 deposits cash at ATM (6 days ago)
        seedTransaction(null, c2Checking.getIban(), "750.00", customer2Email, "ATM cash deposit", TransactionType.DEPOSIT, now.minusDays(6));
        c2Checking.credit(new BigDecimal("750.00"));

        // Customer 1 transfers to Customer 2 (5 days ago)
        seedTransaction(c1Checking.getIban(), c2Checking.getIban(), "200.00", customerEmail, "Dinner reimbursement", TransactionType.TRANSFER, now.minusDays(5));
        c1Checking.debit(new BigDecimal("200.00"));
        c2Checking.credit(new BigDecimal("200.00"));

        // Customer 2 transfers to Customer 1 (4 days ago)
        seedTransaction(c2Checking.getIban(), c1Checking.getIban(), "75.50", customer2Email, "Split groceries", TransactionType.TRANSFER, now.minusDays(4));
        c2Checking.debit(new BigDecimal("75.50"));
        c1Checking.credit(new BigDecimal("75.50"));

        // Customer 1 moves money to own savings (3 days ago)
        seedTransaction(c1Checking.getIban(), c1Savings.getIban(), "300.00", customerEmail, "Monthly savings", TransactionType.TRANSFER, now.minusDays(3));
        c1Checking.debit(new BigDecimal("300.00"));
        c1Savings.credit(new BigDecimal("300.00"));

        // Customer 2 moves money to own savings (3 days ago)
        seedTransaction(c2Checking.getIban(), c2Savings.getIban(), "400.00", customer2Email, "Savings top-up", TransactionType.TRANSFER, now.minusDays(3));
        c2Checking.debit(new BigDecimal("400.00"));
        c2Savings.credit(new BigDecimal("400.00"));

        // Customer 1 ATM withdrawal (2 days ago)
        seedTransaction(c1Checking.getIban(), null, "100.00", customerEmail, "ATM withdrawal", TransactionType.WITHDRAWAL, now.minusDays(2));
        c1Checking.debit(new BigDecimal("100.00"));

        // Customer 2 transfers to Customer 1 (2 days ago)
        seedTransaction(c2Checking.getIban(), c1Checking.getIban(), "150.00", customer2Email, "Concert tickets", TransactionType.TRANSFER, now.minusDays(2));
        c2Checking.debit(new BigDecimal("150.00"));
        c1Checking.credit(new BigDecimal("150.00"));

        // Customer 1 transfers to Customer 2 (1 day ago)
        seedTransaction(c1Checking.getIban(), c2Checking.getIban(), "50.00", customerEmail, "Coffee money", TransactionType.TRANSFER, now.minusDays(1));
        c1Checking.debit(new BigDecimal("50.00"));
        c2Checking.credit(new BigDecimal("50.00"));

        // Customer 2 ATM withdrawal (today)
        seedTransaction(c2Checking.getIban(), null, "60.00", customer2Email, "ATM cash withdrawal", TransactionType.WITHDRAWAL, now.minusHours(3));
        c2Checking.debit(new BigDecimal("60.00"));

        accountRepository.saveAll(List.of(c1Checking, c1Savings, c2Checking, c2Savings));
    }

    private void seedTransaction(String fromIban, String toIban, String amount,
                                 String performedBy, String description, TransactionType type, LocalDateTime timestamp) {
        transactionRepository.save(new Transaction(fromIban, toIban, new BigDecimal(amount), performedBy, description, type, timestamp));
    }

    private String generateUniqueIban() {
        String iban;
        do {
            iban = Iban.random(CountryCode.NL).toString();
        } while (accountRepository.existsById(iban));
        return iban;
    }
}
