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

    @Value("${seed.customer3.email}")
    private String customer3Email;

    @Value("${seed.customer3.password}")
    private String customer3Password;

    @Value("${seed.pending1.email}")
    private String pending1Email;

    @Value("${seed.pending1.password}")
    private String pending1Password;

    @Value("${seed.pending2.email}")
    private String pending2Email;

    @Value("${seed.pending2.password}")
    private String pending2Password;

    @Value("${seed.pending3.email}")
    private String pending3Email;

    @Value("${seed.pending3.password}")
    private String pending3Password;

    @Value("${seed.pending4.email}")
    private String pending4Email;

    @Value("${seed.pending4.password}")
    private String pending4Password;

    public DataInitializer(UserRepository userRepository, AccountRepository accountRepository,
                           TransactionRepository transactionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedIfMissing(employeeEmail, () -> seedEmployee());
        seedIfMissing(customerEmail, () -> seedActiveCustomer("Sanne", "de Vries", customerEmail, customerPassword, "192384756", "0612345678"));
        seedIfMissing(customer2Email, () -> seedActiveCustomer("Bram", "Janssen", customer2Email, customer2Password, "184756321", "0623456789"));
        seedIfMissing(customer3Email, () -> seedActiveCustomer("Eva", "van den Berg", customer3Email, customer3Password, "203847562", "0634567890"));
        seedIfMissing(pending1Email, () -> seedPendingCustomer("Daan", "Bakker", pending1Email, pending1Password, "194827365", "0645678901"));
        seedIfMissing(pending2Email, () -> seedPendingCustomer("Lotte", "Visser", pending2Email, pending2Password, "210394857", "0656789012"));
        seedIfMissing(pending3Email, () -> seedPendingCustomer("Thijs", "Smit", pending3Email, pending3Password, "183746521", "0667890123"));
        seedIfMissing(pending4Email, () -> seedPendingCustomer("Fenna", "Mulder", pending4Email, pending4Password, "172938465", "0678901234"));

        // Seed transactions only when all 3 active customers have accounts
        seedTransactionsIfNeeded();
    }

    private void seedIfMissing(String email, Runnable seeder) {
        if (!userRepository.existsByEmail(email)) seeder.run();
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

    private void seedActiveCustomer(String firstName, String lastName, String email, String password,
                                    String bsn, String phone) {
        User customer = new User(firstName, lastName, email, passwordEncoder.encode(password), bsn, phone, User.Role.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
        customer.setPin(passwordEncoder.encode("1234"));
        customer = userRepository.save(customer);
        accountRepository.saveAll(List.of(
                new Account(accountRepository.generateUniqueIban(), AccountType.CHECKING, new BigDecimal("1500.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), customer),
                new Account(accountRepository.generateUniqueIban(), AccountType.SAVINGS, new BigDecimal("1500.00"), BigDecimal.ZERO, new BigDecimal("500.00"), customer)
        ));
    }

    private void seedPendingCustomer(String firstName, String lastName, String email, String password,
                                     String bsn, String phone) {
        User customer = new User(firstName, lastName, email, passwordEncoder.encode(password), bsn, phone, User.Role.CUSTOMER);
        // status defaults to PENDING — no accounts created
        userRepository.save(customer);
    }

    private void seedTransactionsIfNeeded() {
        if (transactionRepository.count() > 0) return;

        List<String> c1Ibans = accountRepository.findOwnedIbansByUserId(userRepository.findByEmail(customerEmail).orElseThrow().getId());
        List<String> c2Ibans = accountRepository.findOwnedIbansByUserId(userRepository.findByEmail(customer2Email).orElseThrow().getId());
        List<String> c3Ibans = accountRepository.findOwnedIbansByUserId(userRepository.findByEmail(customer3Email).orElseThrow().getId());

        if (c1Ibans.size() < 2 || c2Ibans.size() < 2 || c3Ibans.size() < 2) return;

        List<Account> c1Accounts = c1Ibans.stream().map(iban -> accountRepository.findById(iban).orElseThrow()).toList();
        List<Account> c2Accounts = c2Ibans.stream().map(iban -> accountRepository.findById(iban).orElseThrow()).toList();
        List<Account> c3Accounts = c3Ibans.stream().map(iban -> accountRepository.findById(iban).orElseThrow()).toList();

        seedTransactionHistory(c1Accounts, c2Accounts, c3Accounts);
    }

    private void seedTransactionHistory(List<Account> c1Accounts, List<Account> c2Accounts, List<Account> c3Accounts) {
        Account c1Checking = c1Accounts.get(0);
        Account c1Savings  = c1Accounts.get(1);
        Account c2Checking = c2Accounts.get(0);
        Account c2Savings  = c2Accounts.get(1);
        Account c3Checking = c3Accounts.get(0);
        Account c3Savings  = c3Accounts.get(1);

        LocalDateTime now = LocalDateTime.now();

        // --- Day 14: ATM deposits ---
        seedTransaction(null, c1Checking.getIban(), "2000.00", customerEmail, "ATM cash deposit", TransactionType.DEPOSIT, now.minusDays(14));
        c1Checking.credit(new BigDecimal("2000.00"));

        seedTransaction(null, c2Checking.getIban(), "1800.00", customer2Email, "ATM cash deposit", TransactionType.DEPOSIT, now.minusDays(14));
        c2Checking.credit(new BigDecimal("1800.00"));

        seedTransaction(null, c3Checking.getIban(), "2500.00", customer3Email, "ATM cash deposit", TransactionType.DEPOSIT, now.minusDays(14));
        c3Checking.credit(new BigDecimal("2500.00"));

        // --- Day 12: Customer 1 → Customer 2 transfer ---
        seedTransaction(c1Checking.getIban(), c2Checking.getIban(), "350.00", customerEmail, "Rent share", TransactionType.TRANSFER, now.minusDays(12));
        c1Checking.debit(new BigDecimal("350.00"));
        c2Checking.credit(new BigDecimal("350.00"));

        // --- Day 11: Customer 3 → Customer 1 transfer ---
        seedTransaction(c3Checking.getIban(), c1Checking.getIban(), "120.00", customer3Email, "Birthday gift", TransactionType.TRANSFER, now.minusDays(11));
        c3Checking.debit(new BigDecimal("120.00"));
        c1Checking.credit(new BigDecimal("120.00"));

        // --- Day 10: Customer 2 → Customer 3 transfer ---
        seedTransaction(c2Checking.getIban(), c3Checking.getIban(), "85.00", customer2Email, "Concert tickets", TransactionType.TRANSFER, now.minusDays(10));
        c2Checking.debit(new BigDecimal("85.00"));
        c3Checking.credit(new BigDecimal("85.00"));

        // --- Day 9: Own-account transfers ---
        seedTransaction(c1Checking.getIban(), c1Savings.getIban(), "500.00", customerEmail, "Monthly savings", TransactionType.TRANSFER, now.minusDays(9));
        c1Checking.debit(new BigDecimal("500.00"));
        c1Savings.credit(new BigDecimal("500.00"));

        seedTransaction(c3Checking.getIban(), c3Savings.getIban(), "600.00", customer3Email, "Rainy day fund", TransactionType.TRANSFER, now.minusDays(9));
        c3Checking.debit(new BigDecimal("600.00"));
        c3Savings.credit(new BigDecimal("600.00"));

        // --- Day 8: Customer 1 ATM withdrawal ---
        seedTransaction(c1Checking.getIban(), null, "80.00", customerEmail, "ATM withdrawal", TransactionType.WITHDRAWAL, now.minusDays(8));
        c1Checking.debit(new BigDecimal("80.00"));

        // --- Day 7: Customer 2 → Customer 1 ---
        seedTransaction(c2Checking.getIban(), c1Checking.getIban(), "45.50", customer2Email, "Split groceries", TransactionType.TRANSFER, now.minusDays(7));
        c2Checking.debit(new BigDecimal("45.50"));
        c1Checking.credit(new BigDecimal("45.50"));

        // --- Day 6: Customer 3 deposit + Customer 1 → Customer 3 ---
        seedTransaction(null, c3Checking.getIban(), "300.00", customer3Email, "ATM cash deposit", TransactionType.DEPOSIT, now.minusDays(6));
        c3Checking.credit(new BigDecimal("300.00"));

        seedTransaction(c1Checking.getIban(), c3Checking.getIban(), "200.00", customerEmail, "Dinner reimbursement", TransactionType.TRANSFER, now.minusDays(6));
        c1Checking.debit(new BigDecimal("200.00"));
        c3Checking.credit(new BigDecimal("200.00"));

        // --- Day 5: Customer 2 own-account + Customer 3 → Customer 2 ---
        seedTransaction(c2Checking.getIban(), c2Savings.getIban(), "400.00", customer2Email, "Savings top-up", TransactionType.TRANSFER, now.minusDays(5));
        c2Checking.debit(new BigDecimal("400.00"));
        c2Savings.credit(new BigDecimal("400.00"));

        seedTransaction(c3Checking.getIban(), c2Checking.getIban(), "175.00", customer3Email, "Furniture share", TransactionType.TRANSFER, now.minusDays(5));
        c3Checking.debit(new BigDecimal("175.00"));
        c2Checking.credit(new BigDecimal("175.00"));

        // --- Day 4: Customer 2 withdrawal + Customer 1 → Customer 2 ---
        seedTransaction(c2Checking.getIban(), null, "100.00", customer2Email, "ATM withdrawal", TransactionType.WITHDRAWAL, now.minusDays(4));
        c2Checking.debit(new BigDecimal("100.00"));

        seedTransaction(c1Checking.getIban(), c2Checking.getIban(), "60.00", customerEmail, "Lunch money", TransactionType.TRANSFER, now.minusDays(4));
        c1Checking.debit(new BigDecimal("60.00"));
        c2Checking.credit(new BigDecimal("60.00"));

        // --- Day 3: Customer 3 → Customer 1 + Customer 3 withdrawal ---
        seedTransaction(c3Checking.getIban(), c1Checking.getIban(), "90.00", customer3Email, "Train tickets", TransactionType.TRANSFER, now.minusDays(3));
        c3Checking.debit(new BigDecimal("90.00"));
        c1Checking.credit(new BigDecimal("90.00"));

        seedTransaction(c3Checking.getIban(), null, "50.00", customer3Email, "ATM cash withdrawal", TransactionType.WITHDRAWAL, now.minusDays(3));
        c3Checking.debit(new BigDecimal("50.00"));

        // --- Day 2: Customer 2 → Customer 3 + Customer 1 own-account ---
        seedTransaction(c2Checking.getIban(), c3Checking.getIban(), "130.00", customer2Email, "Utility bills split", TransactionType.TRANSFER, now.minusDays(2));
        c2Checking.debit(new BigDecimal("130.00"));
        c3Checking.credit(new BigDecimal("130.00"));

        seedTransaction(c1Savings.getIban(), c1Checking.getIban(), "150.00", customerEmail, "Move back to checking", TransactionType.TRANSFER, now.minusDays(2));
        c1Savings.debit(new BigDecimal("150.00"));
        c1Checking.credit(new BigDecimal("150.00"));

        // --- Day 1: Customer 3 → Customer 2 + Customer 1 deposit ---
        seedTransaction(c3Checking.getIban(), c2Checking.getIban(), "55.00", customer3Email, "Coffee money", TransactionType.TRANSFER, now.minusDays(1));
        c3Checking.debit(new BigDecimal("55.00"));
        c2Checking.credit(new BigDecimal("55.00"));

        seedTransaction(null, c1Checking.getIban(), "400.00", customerEmail, "ATM cash deposit", TransactionType.DEPOSIT, now.minusDays(1));
        c1Checking.credit(new BigDecimal("400.00"));

        // --- Today: Customer 2 → Customer 1 ---
        seedTransaction(c2Checking.getIban(), c1Checking.getIban(), "75.00", customer2Email, "Book reimbursement", TransactionType.TRANSFER, now.minusHours(3));
        c2Checking.debit(new BigDecimal("75.00"));
        c1Checking.credit(new BigDecimal("75.00"));

        accountRepository.saveAll(List.of(c1Checking, c1Savings, c2Checking, c2Savings, c3Checking, c3Savings));
    }

    private void seedTransaction(String fromIban, String toIban, String amount,
                                 String performedBy, String description, TransactionType type, LocalDateTime timestamp) {
        transactionRepository.save(new Transaction(fromIban, toIban, new BigDecimal(amount), performedBy, description, type, timestamp));
    }

}
