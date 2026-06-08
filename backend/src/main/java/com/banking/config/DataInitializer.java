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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    /**
     * Bump this version whenever seed data changes.
     * On startup the seeder checks for a marker transaction with this description;
     * if missing it wipes all transactions and re-seeds the full history.
     */
    private static final String SEED_VERSION = "SEED_V4";

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
    @Transactional
    public void run(String... args) {
        // 1. Seed users (idempotent — only creates when missing)
        seedIfMissing(employeeEmail, this::seedEmployee);
        seedIfMissing(customerEmail, () -> seedActiveCustomer("Sanne", "de Vries", customerEmail, customerPassword, "192384756", "0612345678"));
        seedIfMissing(customer2Email, () -> seedActiveCustomer("Bram", "Janssen", customer2Email, customer2Password, "184756321", "0623456789"));
        seedIfMissing(customer3Email, () -> seedActiveCustomer("Eva", "van den Berg", customer3Email, customer3Password, "203847562", "0634567890"));
        seedIfMissing(pending1Email, () -> seedPendingCustomer("Daan", "Bakker", pending1Email, pending1Password, "194827365", "0645678901"));
        seedIfMissing(pending2Email, () -> seedPendingCustomer("Lotte", "Visser", pending2Email, pending2Password, "210394857", "0656789012"));
        seedIfMissing(pending3Email, () -> seedPendingCustomer("Thijs", "Smit", pending3Email, pending3Password, "183746521", "0667890123"));
        seedIfMissing(pending4Email, () -> seedPendingCustomer("Fenna", "Mulder", pending4Email, pending4Password, "172938465", "0678901234"));

        // 2. Ensure each active customer has accounts (handles case where user exists but accounts don't)
        ensureAccountsExist(customerEmail);
        ensureAccountsExist(customer2Email);
        ensureAccountsExist(customer3Email);

        // 3. Seed transactions — version-gated: wipes and re-creates when SEED_VERSION changes
        seedTransactionsIfVersionChanged();
    }

    // ---- helpers ----

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
                new Account(accountRepository.generateUniqueIban(), AccountType.CHECKING, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("2000.00"), customer),
                new Account(accountRepository.generateUniqueIban(), AccountType.SAVINGS, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("500.00"), customer)
        ));
    }

    private void seedPendingCustomer(String firstName, String lastName, String email, String password,
                                     String bsn, String phone) {
        User customer = new User(firstName, lastName, email, passwordEncoder.encode(password), bsn, phone, User.Role.CUSTOMER);
        // status defaults to PENDING — no accounts created
        userRepository.save(customer);
    }

    private void ensureAccountsExist(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getStatus() != UserStatus.ACTIVE) return;
        List<String> ibans = accountRepository.findOwnedIbansByUserId(user.getId());
        if (ibans.isEmpty()) {
            accountRepository.saveAll(List.of(
                    new Account(accountRepository.generateUniqueIban(), AccountType.CHECKING, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("2000.00"), user),
                    new Account(accountRepository.generateUniqueIban(), AccountType.SAVINGS, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("500.00"), user)
            ));
        }
    }

    // ---- transaction seeding ----

    private void seedTransactionsIfVersionChanged() {
        if (transactionRepository.existsByDescription(SEED_VERSION)) return;

        // Wipe stale transactions so the full history is consistent
        transactionRepository.deleteAll();

        // Look up accounts for all 3 active customers
        List<Account> c1 = loadAccounts(customerEmail);
        List<Account> c2 = loadAccounts(customer2Email);
        List<Account> c3 = loadAccounts(customer3Email);

        if (c1.size() < 2 || c2.size() < 2 || c3.size() < 2) return;

        Account c1C = c1.get(0), c1S = c1.get(1);
        Account c2C = c2.get(0), c2S = c2.get(1);
        Account c3C = c3.get(0), c3S = c3.get(1);

        // Reset all balances to zero before replaying history
        resetBalance(c1C); resetBalance(c1S);
        resetBalance(c2C); resetBalance(c2S);
        resetBalance(c3C); resetBalance(c3S);

        seedFullHistory(c1C, c1S, c2C, c2S, c3C, c3S);

        // Persist updated balances
        accountRepository.saveAll(List.of(c1C, c1S, c2C, c2S, c3C, c3S));

        // Write version marker (hidden from normal queries by its zero amount)
        transactionRepository.save(new Transaction(null, null, BigDecimal.ZERO,
                "system", SEED_VERSION, TransactionType.DEPOSIT, LocalDateTime.now().minusDays(31)));
    }

    private List<Account> loadAccounts(String email) {
        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        List<Account> accounts = accountRepository.findByUserIdAndActiveTrue(userId);
        // Sort so CHECKING always comes before SAVINGS (deterministic ordering)
        accounts.sort((a, b) -> a.getAccountType().compareTo(b.getAccountType()));
        return accounts;
    }

    private void resetBalance(Account account) {
        BigDecimal bal = account.getBalance();
        if (bal.signum() > 0) account.debit(bal);
        else if (bal.signum() < 0) account.credit(bal.negate());
    }

    // ---- shorthand helpers ----

    private void deposit(Account to, String amount, String email, String desc, LocalDateTime ts) {
        BigDecimal amt = new BigDecimal(amount);
        transactionRepository.save(new Transaction(null, to.getIban(), amt, email, desc, TransactionType.DEPOSIT, ts));
        to.credit(amt);
    }

    private void withdraw(Account from, String amount, String email, String desc, LocalDateTime ts) {
        BigDecimal amt = new BigDecimal(amount);
        transactionRepository.save(new Transaction(from.getIban(), null, amt, email, desc, TransactionType.WITHDRAWAL, ts));
        from.debit(amt);
    }

    private void transfer(Account from, Account to, String amount, String email, String desc, LocalDateTime ts) {
        BigDecimal amt = new BigDecimal(amount);
        transactionRepository.save(new Transaction(from.getIban(), to.getIban(), amt, email, desc, TransactionType.TRANSFER, ts));
        from.debit(amt);
        to.credit(amt);
    }

    // ---- full transaction history (20+ per account, ~115 total) ----

    private void seedFullHistory(Account c1C, Account c1S, Account c2C, Account c2S, Account c3C, Account c3S) {
        LocalDateTime now = LocalDateTime.now();

        // ========== Day 30: Salary deposits ==========
        deposit(c1C, "5000.00", customerEmail,  "Salary deposit",  now.minusDays(30));
        deposit(c2C, "4500.00", customer2Email, "Salary deposit",  now.minusDays(30));
        deposit(c3C, "5200.00", customer3Email, "Salary deposit",  now.minusDays(30));

        // ========== Day 29: Initial savings ==========
        deposit(c1S, "2000.00", customerEmail,  "Initial savings transfer", now.minusDays(29));
        deposit(c2S, "1800.00", customer2Email, "Initial savings transfer", now.minusDays(29));
        deposit(c3S, "2200.00", customer3Email, "Initial savings transfer", now.minusDays(29));

        // ========== Day 28: First cross-customer transfers ==========
        transfer(c1C, c2C, "350.00", customerEmail,  "Rent share",       now.minusDays(28));
        transfer(c2C, c3C, "175.00", customer2Email, "Concert tickets",  now.minusDays(28));
        transfer(c3C, c1C, "200.00", customer3Email, "Birthday gift",    now.minusDays(28));

        // ========== Day 27: Own-account transfers ==========
        transfer(c1C, c1S, "800.00", customerEmail,  "Monthly savings",  now.minusDays(27));
        transfer(c2C, c2S, "600.00", customer2Email, "Savings top-up",   now.minusDays(27));
        transfer(c3C, c3S, "700.00", customer3Email, "Emergency fund",   now.minusDays(27));

        // ========== Day 26: ATM + transfers ==========
        withdraw(c1C, "100.00", customerEmail,  "ATM withdrawal",    now.minusDays(26));
        withdraw(c2C, "80.00",  customer2Email, "ATM withdrawal",    now.minusDays(26));
        withdraw(c3C, "120.00", customer3Email, "ATM withdrawal",    now.minusDays(26));
        transfer(c2C, c1C, "45.50", customer2Email, "Split groceries", now.minusDays(26));

        // ========== Day 25: Mixed activity ==========
        transfer(c1C, c3C, "200.00", customerEmail,  "Dinner reimbursement", now.minusDays(25));
        transfer(c3C, c2C, "85.00",  customer3Email, "Utility share",        now.minusDays(25));
        deposit(c1C, "400.00", customerEmail, "Freelance payment", now.minusDays(25));

        // ========== Day 24: Transfers + salary ==========
        transfer(c1C, c2C, "60.00",  customerEmail,  "Lunch money",    now.minusDays(24));
        transfer(c3C, c1C, "90.00",  customer3Email, "Train tickets",  now.minusDays(24));
        deposit(c2C, "1200.00", customer2Email, "Salary deposit",      now.minusDays(24));

        // ========== Day 23: Savings ↔ checking ==========
        transfer(c2C, c3C, "130.00", customer2Email, "Utility bills split",    now.minusDays(23));
        transfer(c1S, c1C, "150.00", customerEmail,  "Move back to checking",  now.minusDays(23));
        transfer(c2S, c2C, "200.00", customer2Email, "Spending money",         now.minusDays(23));

        // ========== Day 22: Cross-customer + deposit ==========
        transfer(c3C, c2C, "55.00",  customer3Email, "Coffee money",       now.minusDays(22));
        deposit(c1C, "400.00", customerEmail, "Client payment",            now.minusDays(22));
        transfer(c2C, c1C, "75.00",  customer2Email, "Book reimbursement", now.minusDays(22));

        // ========== Day 21: Larger movements ==========
        transfer(c1C, c3C, "300.00", customerEmail,  "Gift",           now.minusDays(21));
        transfer(c3C, c3S, "400.00", customer3Email, "Save extra",     now.minusDays(21));
        deposit(c3C, "800.00", customer3Email, "Salary deposit",       now.minusDays(21));

        // ========== Day 20: Savings round ==========
        transfer(c1C, c1S, "500.00", customerEmail,  "Extra savings",     now.minusDays(20));
        transfer(c2C, c2S, "350.00", customer2Email, "Savings goal",      now.minusDays(20));
        transfer(c3S, c3C, "150.00", customer3Email, "Need for bills",    now.minusDays(20));

        // ========== Day 19: Withdrawals + transfers ==========
        withdraw(c1C, "60.00",  customerEmail,  "ATM cash",               now.minusDays(19));
        withdraw(c2C, "50.00",  customer2Email, "ATM cash",               now.minusDays(19));
        transfer(c1C, c2C, "95.00",  customerEmail,  "Gym membership split", now.minusDays(19));
        transfer(c3C, c1C, "65.00",  customer3Email, "Parking reimbursement", now.minusDays(19));

        // ========== Day 18: Savings cross-transfers ==========
        transfer(c2C, c3C, "110.00", customer2Email, "Electronics share",     now.minusDays(18));
        deposit(c2S, "500.00", customer2Email, "Bonus deposit",               now.minusDays(18));
        transfer(c1S, c2S, "200.00", customerEmail,  "Savings transfer",      now.minusDays(18));
        deposit(c1S, "300.00", customerEmail, "Interest deposit",             now.minusDays(18));

        // ========== Day 17: More cross-customer ==========
        transfer(c3C, c1C, "180.00", customer3Email, "Furniture split",       now.minusDays(17));
        transfer(c1C, c2C, "140.00", customerEmail,  "Insurance share",       now.minusDays(17));
        deposit(c3S, "400.00", customer3Email, "Savings deposit",             now.minusDays(17));
        transfer(c2S, c3S, "150.00", customer2Email, "Savings gift",          now.minusDays(17));

        // ========== Day 16: Withdrawals + rent ==========
        withdraw(c3C, "90.00",  customer3Email, "ATM withdrawal",             now.minusDays(16));
        transfer(c2C, c1C, "250.00", customer2Email, "Rent refund",           now.minusDays(16));
        transfer(c3C, c2C, "95.00",  customer3Email, "Streaming subscription", now.minusDays(16));
        deposit(c1S, "250.00", customerEmail, "Monthly auto-save",            now.minusDays(16));

        // ========== Day 15: Savings movements ==========
        transfer(c1C, c3C, "75.00",  customerEmail,  "Movie tickets",         now.minusDays(15));
        transfer(c3S, c1S, "100.00", customer3Email, "Savings return",        now.minusDays(15));
        transfer(c2S, c2C, "175.00", customer2Email, "Move to checking",      now.minusDays(15));
        deposit(c2S, "300.00", customer2Email, "Savings auto-deposit",        now.minusDays(15));

        // ========== Day 14: Emergency + year-end ==========
        transfer(c1S, c1C, "200.00", customerEmail,  "Emergency expense",     now.minusDays(14));
        deposit(c3S, "350.00", customer3Email, "Gift savings",                now.minusDays(14));
        transfer(c2C, c2S, "450.00", customer2Email, "Year-end savings",      now.minusDays(14));
        transfer(c3C, c3S, "300.00", customer3Email, "Monthly savings",       now.minusDays(14));

        // ========== Day 13: Cross-customer + savings ==========
        transfer(c1C, c2C, "85.00",  customerEmail,  "Lunch split",           now.minusDays(13));
        transfer(c3C, c1C, "120.00", customer3Email, "Travel expenses",       now.minusDays(13));
        transfer(c2S, c3S, "125.00", customer2Email, "Savings exchange",      now.minusDays(13));
        deposit(c1S, "200.00", customerEmail, "Dividend",                     now.minusDays(13));

        // ========== Day 12: More transfers ==========
        transfer(c2C, c3C, "65.00",  customer2Email, "Snack money",           now.minusDays(12));
        transfer(c1C, c3C, "180.00", customerEmail,  "Computer repair share", now.minusDays(12));
        transfer(c3S, c2S, "90.00",  customer3Email, "Savings back",          now.minusDays(12));
        deposit(c2S, "150.00", customer2Email, "Interest",                    now.minusDays(12));

        // ========== Day 11: Appliance + savings ==========
        withdraw(c1C, "75.00",  customerEmail,  "Cash withdrawal",            now.minusDays(11));
        transfer(c3C, c2C, "200.00", customer3Email, "Appliance split",       now.minusDays(11));
        transfer(c1S, c3S, "175.00", customerEmail,  "Savings transfer",      now.minusDays(11));
        transfer(c2S, c1S, "160.00", customer2Email, "Savings return",        now.minusDays(11));

        // ========== Day 10: Gas + savings ==========
        transfer(c2C, c1C, "110.00", customer2Email, "Gas money",             now.minusDays(10));
        deposit(c3C, "500.00", customer3Email, "Freelance payment",           now.minusDays(10));
        transfer(c1S, c2S, "100.00", customerEmail,  "Savings tip",           now.minusDays(10));
        transfer(c3S, c1S, "200.00", customer3Email, "Savings refund",        now.minusDays(10));

        // ========== Day 9: Weekly savings ==========
        transfer(c1C, c1S, "350.00", customerEmail,  "Weekly savings",        now.minusDays(9));
        transfer(c3C, c1C, "95.00",  customer3Email, "Taxi split",            now.minusDays(9));
        transfer(c2C, c3C, "85.00",  customer2Email, "Coffee run",            now.minusDays(9));
        transfer(c2S, c2C, "250.00", customer2Email, "Need for rent",         now.minusDays(9));

        // ========== Day 8: Mixed day ==========
        deposit(c1S, "175.00", customerEmail, "Cashback reward",              now.minusDays(8));
        withdraw(c2C, "65.00",  customer2Email, "ATM cash",                   now.minusDays(8));
        transfer(c3S, c3C, "180.00", customer3Email, "Bill payment fund",     now.minusDays(8));
        transfer(c1C, c2C, "55.00",  customerEmail,  "App subscription",      now.minusDays(8));

        // ========== Day 7: Loan + savings ==========
        transfer(c3C, c2C, "45.00",  customer3Email, "Snack split",           now.minusDays(7));
        transfer(c2C, c1C, "160.00", customer2Email, "Loan repayment",        now.minusDays(7));
        deposit(c3S, "200.00", customer3Email, "Savings boost",               now.minusDays(7));
        transfer(c1S, c1C, "100.00", customerEmail,  "Petrol money",          now.minusDays(7));

        // ========== Day 6: Savings swaps ==========
        deposit(c2S, "225.00", customer2Email, "Savings top-up",              now.minusDays(6));
        transfer(c1C, c3C, "140.00", customerEmail,  "Birthday dinner",       now.minusDays(6));
        transfer(c3S, c2S, "135.00", customer3Email, "Savings swap",          now.minusDays(6));
        transfer(c2C, c2S, "200.00", customer2Email, "Quick save",            now.minusDays(6));

        // ========== Day 5: Movies + savings ==========
        transfer(c2C, c3C, "70.00",  customer2Email, "Movie night",           now.minusDays(5));
        transfer(c3C, c3S, "250.00", customer3Email, "Big savings",           now.minusDays(5));
        transfer(c1S, c2S, "110.00", customerEmail,  "Help savings",          now.minusDays(5));
        deposit(c1C, "600.00", customerEmail, "Salary deposit",               now.minusDays(5));

        // ========== Day 4: Vacation + gym ==========
        transfer(c1C, c2C, "180.00", customerEmail,  "Vacation fund",         now.minusDays(4));
        transfer(c3C, c1C, "110.00", customer3Email, "Gym split",             now.minusDays(4));
        withdraw(c3C, "40.00",  customer3Email, "ATM withdrawal",             now.minusDays(4));
        transfer(c2S, c3S, "100.00", customer2Email, "Savings gift",          now.minusDays(4));
        transfer(c1S, c1C, "75.00",  customerEmail,  "Misc expense",          now.minusDays(4));

        // ========== Day 3: Freelance + books ==========
        deposit(c3C, "350.00", customer3Email, "Freelance payment",           now.minusDays(3));
        transfer(c2C, c1C, "90.00",  customer2Email, "Study books",           now.minusDays(3));
        transfer(c3S, c1S, "85.00",  customer3Email, "Savings return",        now.minusDays(3));
        deposit(c2C, "450.00", customer2Email, "Salary deposit",              now.minusDays(3));

        // ========== Day 2: Groceries + saves ==========
        transfer(c1C, c3C, "65.00",  customerEmail,  "Takeout",               now.minusDays(2));
        transfer(c3C, c2C, "120.00", customer3Email, "Groceries split",       now.minusDays(2));
        transfer(c2C, c2S, "300.00", customer2Email, "Savings push",          now.minusDays(2));
        deposit(c3S, "150.00", customer3Email, "Auto-save",                   now.minusDays(2));
        withdraw(c1C, "50.00",  customerEmail,  "ATM withdrawal",             now.minusDays(2));

        // ========== Day 1: End of month ==========
        transfer(c3C, c1C, "75.00",  customer3Email, "Parking split",         now.minusDays(1));
        transfer(c2C, c3C, "95.00",  customer2Email, "Dinner out",            now.minusDays(1));
        transfer(c1C, c1S, "200.00", customerEmail,  "End of month savings",  now.minusDays(1));

        // ========== Today ==========
        transfer(c1C, c2C, "40.00",  customerEmail,  "Coffee",                now.minusHours(5));
        deposit(c3C, "250.00", customer3Email, "Online refund",               now.minusHours(3));
        transfer(c2C, c1C, "55.00",  customer2Email, "Lunch split",           now.minusHours(1));
    }
}
