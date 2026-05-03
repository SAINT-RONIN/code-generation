package com.banking.config;

import com.banking.model.User;
import com.banking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("employee@bank.com")) {
            seedEmployee();
        }
    }

    private void seedEmployee() {
        User employee = new User(
                "Bank", "Employee",
                "employee@bank.com",
                passwordEncoder.encode("employee123"),
                "000000000", "0600000000",
                User.Role.EMPLOYEE
        );
        userRepository.save(employee);
    }
}
