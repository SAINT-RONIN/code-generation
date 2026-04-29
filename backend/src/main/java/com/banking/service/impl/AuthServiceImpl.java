package com.banking.service.impl;

import com.banking.dto.RegisterRequest;
import com.banking.exception.EmailAlreadyInUseException;
import com.banking.model.User;
import com.banking.repository.UserRepository;
import com.banking.service.interfaces.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @SuppressWarnings("null")
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }

        User newCustomer = buildCustomerFrom(request);
        return userRepository.save(newCustomer);
    }

    private User buildCustomerFrom(RegisterRequest request) {
        return new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.bsn(),
                request.phoneNumber(),
                User.Role.CUSTOMER
        );
    }
}
